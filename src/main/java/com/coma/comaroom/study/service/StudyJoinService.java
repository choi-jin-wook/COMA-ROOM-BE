package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.entity.*;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyJoinService {
    private final StudyRepository studies;
    private final StudyMemberRepository memberships;
    private final StudyJoinRequestRepository requests;
    private final StudyAccessService access;
    private final SecurityUtils security;

    public StudyPage<RecruitingStudyResponse> recruiting(int page, int size) {
        Long memberId = security.getCurrentMember().getMemberId();
        return StudyPage.from(studies.findRecruiting(memberId, StudyAccessService.page(page, size, "id"))
                .map(study -> new RecruitingStudyResponse(study.getId(), study.getStudyName(),
                        study.getDescription(), access.currentMembers(study), study.getMaxMembers(),
                        study.getScheduleDescription(), study.getLevel(), study.getTags(),
                        new RecruitingStudyResponse.Manager(study.getStudyManager().getMemberId(),
                                study.getStudyManager().getName()),
                        requests.findByStudyIdAndMemberMemberId(study.getId(), memberId)
                                .map(request -> request.getStatus().name()).orElse("NONE"))));
    }

    @Transactional
    public StudyJoinRequestResponse request(Long studyId) {
        Member member = security.getCurrentMember();
        Study study = access.lockStudy(studyId);
        if (access.isLeader(study, member.getMemberId()) ||
                memberships.existsByStudyIdAndMemberMemberId(studyId, member.getMemberId())) {
            throw new BusinessException(StudyError.JOIN_CONFLICT);
        }
        if (study.getStatus() != StudyStatus.ACTIVE || study.getStudyManager() == null ||
                (study.getMaxMembers() != null && access.currentMembers(study) >= study.getMaxMembers())) {
            throw new BusinessException(StudyError.STUDY_CLOSED);
        }
        StudyJoinRequest request = requests.findByStudyIdAndMemberMemberId(studyId, member.getMemberId())
                .orElseGet(() -> StudyJoinRequest.builder().study(study).member(member).build());
        if (request.getId() != null && request.getStatus() == StudyJoinRequest.Status.PENDING) {
            throw new BusinessException(StudyError.JOIN_CONFLICT);
        }
        request.requestAgain();
        return StudyJoinRequestResponse.from(requests.save(request));
    }
}
