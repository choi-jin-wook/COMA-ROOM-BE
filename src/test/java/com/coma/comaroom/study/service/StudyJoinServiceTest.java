package com.coma.comaroom.study.service;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.entity.*;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudyJoinServiceTest {
    private final StudyRepository studies = mock(StudyRepository.class);
    private final StudyMemberRepository memberships = mock(StudyMemberRepository.class);
    private final StudyJoinRequestRepository requests = mock(StudyJoinRequestRepository.class);
    private final SecurityUtils security = mock(SecurityUtils.class);
    private final StudyJoinService service = new StudyJoinService(studies, memberships, requests,
            new StudyAccessService(studies, memberships), security);

    private Study setupStudy() {
        Member applicant = Member.builder().memberId(2L).build();
        Study study = Study.builder().id(1L).studyManager(Member.builder().memberId(3L).build()).build();
        when(security.getCurrentMember()).thenReturn(applicant);
        when(studies.findForUpdate(1L)).thenReturn(Optional.of(study));
        return study;
    }

    @Test
    void createsPendingRequestWithoutMembershipOrXpWrites() {
        setupStudy();
        when(requests.save(any())).thenAnswer(call -> call.getArgument(0));
        assertThat(service.request(1L).status()).isEqualTo(StudyJoinRequest.Status.PENDING);
        verify(memberships, never()).save(any());
    }

    @Test
    void rejectsDuplicatePendingRequestAndExistingMember() {
        setupStudy();
        when(requests.findByStudyIdAndMemberMemberId(1L, 2L)).thenReturn(Optional.of(
                StudyJoinRequest.builder().id(5L).build()));
        assertThatThrownBy(() -> service.request(1L)).hasMessage(StudyError.JOIN_CONFLICT.getMessage());
        when(memberships.existsByStudyIdAndMemberMemberId(1L, 2L)).thenReturn(true);
        assertThatThrownBy(() -> service.request(1L)).hasMessage(StudyError.JOIN_CONFLICT.getMessage());
        verify(requests, never()).save(any());
    }

    @Test
    void rejectsFullAndCompletedStudies() {
        Study study = setupStudy();
        study.setMaxMembers(1);
        assertThatThrownBy(() -> service.request(1L)).hasMessage(StudyError.STUDY_CLOSED.getMessage());
        study.setMaxMembers(null);
        study.setStatus(StudyStatus.COMPLETED);
        assertThatThrownBy(() -> service.request(1L)).hasMessage(StudyError.STUDY_CLOSED.getMessage());
        verify(requests, never()).save(any());
    }
}
