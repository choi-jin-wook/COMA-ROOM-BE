package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.request.AddStudyActivityRequest;
import com.coma.comaroom.study.dto.request.AddStudyMemberRequest;
import com.coma.comaroom.study.dto.request.CreateStudyRequest;
import com.coma.comaroom.study.dto.response.StudyActivityResponse;
import com.coma.comaroom.study.dto.response.StudyResponse;
import com.coma.comaroom.study.entity.Study;
import com.coma.comaroom.study.entity.StudyActivity;
import com.coma.comaroom.study.entity.StudyMember;
import com.coma.comaroom.study.repository.StudyActivityRepository;
import com.coma.comaroom.study.repository.StudyMemberRepository;
import com.coma.comaroom.study.repository.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyManagerServiceTest {

    @Mock private StudyRepository studyRepository;
    @Mock private StudyMemberRepository studyMemberRepository;
    @Mock private StudyActivityRepository studyActivityRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks
    private StudyManagerService studyManagerService;

    private Member manager;
    private Member newMember;
    private Study study;
    private StudyActivity studyActivity;

    @BeforeEach
    void setUp() {
        manager = Member.builder()
                .memberId(1L)
                .studentId("20210001")
                .name("스터디장")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        newMember = Member.builder()
                .memberId(2L)
                .studentId("20210002")
                .name("신규멤버")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        study = Study.builder()
                .id(1L)
                .studyName("알고리즘 스터디")
                .studyManager(manager)
                .build();

        studyActivity = StudyActivity.builder()
                .id(1L)
                .activityName("1주차 발표")
                .study(study)
                .build();
    }

    // ─────────────────────────────────────────────
    // createStudy
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("스터디 생성 성공")
    void createStudy_success() {
        CreateStudyRequest request = new CreateStudyRequest("알고리즘 스터디", 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(studyRepository.save(any(Study.class))).thenReturn(study);

        StudyResponse result = studyManagerService.createStudy(request);

        assertThat(result).isNotNull();
        assertThat(result.studyName()).isEqualTo("알고리즘 스터디");
        assertThat(result.managerName()).isEqualTo("스터디장");

        ArgumentCaptor<Study> captor = ArgumentCaptor.forClass(Study.class);
        verify(studyRepository).save(captor.capture());
        assertThat(captor.getValue().getStudyName()).isEqualTo("알고리즘 스터디");
        assertThat(captor.getValue().getStudyManager()).isSameAs(manager);
    }

    @Test
    @DisplayName("스터디 생성 실패 - 매니저 없음")
    void createStudy_managerNotFound() {
        CreateStudyRequest request = new CreateStudyRequest("알고리즘 스터디", 99L);
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyManagerService.createStudy(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.MEMBER_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // addStudyMember
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("스터디 멤버 추가 성공")
    void addStudyMember_success() {
        AddStudyMemberRequest request = new AddStudyMemberRequest(2L);
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(newMember));
        when(studyMemberRepository.existsByStudyIdAndMemberMemberId(1L, 2L)).thenReturn(false);

        assertThatNoException().isThrownBy(() -> studyManagerService.addStudyMember(1L, request));

        ArgumentCaptor<StudyMember> captor = ArgumentCaptor.forClass(StudyMember.class);
        verify(studyMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getStudy()).isSameAs(study);
        assertThat(captor.getValue().getMember()).isSameAs(newMember);
    }

    @Test
    @DisplayName("스터디 멤버 추가 실패 - 스터디 없음")
    void addStudyMember_studyNotFound() {
        AddStudyMemberRequest request = new AddStudyMemberRequest(2L);
        when(studyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyManagerService.addStudyMember(99L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.STUDY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("스터디 멤버 추가 실패 - 멤버 없음")
    void addStudyMember_memberNotFound() {
        AddStudyMemberRequest request = new AddStudyMemberRequest(99L);
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyManagerService.addStudyMember(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("스터디 멤버 추가 실패 - 이미 스터디 멤버")
    void addStudyMember_alreadyMember() {
        AddStudyMemberRequest request = new AddStudyMemberRequest(2L);
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(newMember));
        when(studyMemberRepository.existsByStudyIdAndMemberMemberId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> studyManagerService.addStudyMember(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.ALREADY_STUDY_MEMBER.getMessage());
    }

    // ─────────────────────────────────────────────
    // removeStudyMember
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("스터디 멤버 삭제 성공")
    void removeStudyMember_success() {
        StudyMember studyMember = StudyMember.builder()
                .study(study)
                .member(newMember)
                .build();

        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(studyMemberRepository.findByStudyIdAndMemberMemberId(1L, 2L)).thenReturn(Optional.of(studyMember));

        assertThatNoException().isThrownBy(() -> studyManagerService.removeStudyMember(1L, 2L));
        verify(studyMemberRepository).delete(studyMember);
    }

    @Test
    @DisplayName("스터디 멤버 삭제 실패 - 스터디 없음")
    void removeStudyMember_studyNotFound() {
        when(studyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyManagerService.removeStudyMember(99L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.STUDY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("스터디 멤버 삭제 실패 - 스터디 멤버 아님")
    void removeStudyMember_notMember() {
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(studyMemberRepository.findByStudyIdAndMemberMemberId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyManagerService.removeStudyMember(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.NOT_STUDY_MEMBER.getMessage());
    }

    // ─────────────────────────────────────────────
    // addStudyActivity
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("스터디 일정 추가 성공")
    void addStudyActivity_success() {
        AddStudyActivityRequest request = new AddStudyActivityRequest("1주차 발표");
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(studyActivityRepository.save(any(StudyActivity.class))).thenReturn(studyActivity);

        StudyActivityResponse result = studyManagerService.addStudyActivity(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.activityName()).isEqualTo("1주차 발표");

        ArgumentCaptor<StudyActivity> captor = ArgumentCaptor.forClass(StudyActivity.class);
        verify(studyActivityRepository).save(captor.capture());
        assertThat(captor.getValue().getActivityName()).isEqualTo("1주차 발표");
        assertThat(captor.getValue().getStudy()).isSameAs(study);
    }

    @Test
    @DisplayName("스터디 일정 추가 실패 - 스터디 없음")
    void addStudyActivity_studyNotFound() {
        AddStudyActivityRequest request = new AddStudyActivityRequest("1주차 발표");
        when(studyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyManagerService.addStudyActivity(99L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.STUDY_NOT_FOUND.getMessage());
    }

    // ─────────────────────────────────────────────
    // removeStudyActivity
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("스터디 일정 삭제 성공")
    void removeStudyActivity_success() {
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(studyActivityRepository.findById(1L)).thenReturn(Optional.of(studyActivity));

        assertThatNoException().isThrownBy(() -> studyManagerService.removeStudyActivity(1L, 1L));
        verify(studyActivityRepository).delete(studyActivity);
    }

    @Test
    @DisplayName("스터디 일정 삭제 실패 - 스터디 없음")
    void removeStudyActivity_studyNotFound() {
        when(studyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyManagerService.removeStudyActivity(99L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.STUDY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("스터디 일정 삭제 실패 - 일정 없음")
    void removeStudyActivity_activityNotFound() {
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(studyActivityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyManagerService.removeStudyActivity(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.ACTIVITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("스터디 일정 삭제 실패 - 해당 스터디의 일정이 아님")
    void removeStudyActivity_activityNotInStudy() {
        Study anotherStudy = Study.builder()
                .id(2L)
                .studyName("다른 스터디")
                .studyManager(manager)
                .build();
        StudyActivity activityOfAnotherStudy = StudyActivity.builder()
                .id(2L)
                .activityName("다른 스터디 일정")
                .study(anotherStudy)
                .build();

        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(studyActivityRepository.findById(2L)).thenReturn(Optional.of(activityOfAnotherStudy));

        assertThatThrownBy(() -> studyManagerService.removeStudyActivity(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.ACTIVITY_NOT_IN_STUDY.getMessage());
    }
}
