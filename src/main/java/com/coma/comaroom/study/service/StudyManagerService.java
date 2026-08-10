package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Member;
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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class StudyManagerService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyActivityRepository studyActivityRepository;
    private final MemberRepository memberRepository;

    // 스터디 생성
    public StudyResponse createStudy(CreateStudyRequest request) {
        Member manager = memberRepository.findById(request.getManagerId())
                .orElseThrow(() -> new BusinessException(StudyError.MEMBER_NOT_FOUND));
        Study study = request.toEntity(manager);
        return StudyResponse.from(studyRepository.save(study));
    }

    // 스터디 멤버 추가
    public void addStudyMember(Long studyId, AddStudyMemberRequest request) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new BusinessException(StudyError.MEMBER_NOT_FOUND));
        if (studyMemberRepository.existsByStudyIdAndMemberMemberId(studyId, request.getMemberId())) {
            throw new BusinessException(StudyError.ALREADY_STUDY_MEMBER);
        }
        studyMemberRepository.save(request.toEntity(study, member));
    }

    // 스터디 멤버 삭제
    public void removeStudyMember(Long studyId, Long memberId) {
        studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
        StudyMember studyMember = studyMemberRepository.findByStudyIdAndMemberMemberId(studyId, memberId)
                .orElseThrow(() -> new BusinessException(StudyError.NOT_STUDY_MEMBER));
        studyMemberRepository.delete(studyMember);
    }

    // 스터디 일정 추가
    public StudyActivityResponse addStudyActivity(Long studyId, AddStudyActivityRequest request) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
        StudyActivity activity = request.toEntity(study);
        return StudyActivityResponse.from(studyActivityRepository.save(activity));
    }

    // 스터디 일정 삭제
    public void removeStudyActivity(Long studyId, Long activityId) {
        studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
        StudyActivity activity = studyActivityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(StudyError.ACTIVITY_NOT_FOUND));
        if (!activity.getStudy().getId().equals(studyId)) {
            throw new BusinessException(StudyError.ACTIVITY_NOT_IN_STUDY);
        }
        studyActivityRepository.delete(activity);
    }
}
