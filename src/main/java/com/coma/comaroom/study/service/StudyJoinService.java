package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.entity.*;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 모집 목록과 로그인 회원의 스터디 참여 신청을 처리한다. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyJoinService {
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyJoinRequestRepository studyJoinRequestRepository;
    private final StudyAccessService studyAccessService;
    private final SecurityUtils security;

    /** 이미 참여 중인 스터디를 제외하고 현재 회원의 신청 상태를 함께 반환한다. */
    public Page<RecruitingStudyResponse> recruiting(int page, int size) {
        Long memberId = security.getCurrentMember().getMemberId();
        return studyRepository.findRecruiting(memberId, StudyAccessService.page(page, size, "id"))
                .map(study -> new RecruitingStudyResponse(study.getId(), study.getStudyName(),
                        study.getDescription(), studyAccessService.currentMembers(study),
                        study.getScheduleDescription(), study.getTopic(),
                        new RecruitingStudyResponse.Manager(study.getStudyManager().getMemberId(),
                                study.getStudyManager().getName()),
                        studyJoinRequestRepository.findByStudyIdAndMemberMemberId(study.getId(), memberId)
                                .map(request -> request.getStatus().name()).orElse("NONE")));
    }

    /** 스터디장과 중복 신청을 잠금 상태에서 확인한 뒤 대기 신청을 저장한다. */
    @Transactional
    public StudyJoinRequestResponse request(Long studyId) {
        Member member = security.getCurrentMember();
        Study study = studyAccessService.lockStudy(studyId);
        if (studyAccessService.isLeader(study, member.getMemberId()) ||
                studyMemberRepository.existsByStudyIdAndMemberMemberId(studyId, member.getMemberId())) {
            throw new BusinessException(StudyError.JOIN_CONFLICT);
        }
        if (study.getStudyManager() == null) {
            throw new BusinessException(StudyError.STUDY_CLOSED);
        }
        // 대기 중이 아닌 기존 신청은 새 행을 만들지 않고 다시 대기 상태로 전환한다.
        StudyJoinRequest request = studyJoinRequestRepository.findByStudyIdAndMemberMemberId(studyId, member.getMemberId())
                .orElseGet(() -> StudyJoinRequest.builder().study(study).member(member).build());
        if (request.getId() != null && request.getStatus() == StudyJoinRequest.Status.PENDING) {
            throw new BusinessException(StudyError.JOIN_CONFLICT);
        }
        request.requestAgain();
        return StudyJoinRequestResponse.from(studyJoinRequestRepository.save(request));
    }
}
