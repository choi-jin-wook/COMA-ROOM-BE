package com.coma.comaroom.study.service;

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
public class StudyService {
    private final StudyRepository studies;
    private final StudyMemberRepository memberships;
    private final StudyManagerCandidateRepository candidates;
    private final StudyAccessService access;
    private final SecurityUtils security;

    public StudyPage<StudyManagerCandidate> managers(String keyword, int page, int size) {
        String search = keyword == null ? "" : keyword.strip();
        return StudyPage.from(candidates.findByNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(
                search, search, StudyAccessService.page(page, size, "memberId")).map(StudyManagerCandidate::from));
    }

    public StudySummary summary() {
        Long memberId = security.getCurrentMember().getMemberId();
        return new StudySummary(studies.countMine(memberId, StudyStatus.ACTIVE),
                memberships.totalEarnedXp(memberId), studies.countMine(memberId, StudyStatus.COMPLETED));
    }

    public StudyPage<MyStudyResponse> mine(StudyStatus status, int page, int size) {
        Long memberId = security.getCurrentMember().getMemberId();
        return StudyPage.from(studies.findMine(memberId, status, StudyAccessService.page(page, size, "id"))
                .map(study -> new MyStudyResponse(study.getId(), study.getStudyName(), study.getDescription(),
                        access.isLeader(study, memberId) ? "LEADER" : "MEMBER", study.getStatus(), null,
                        access.currentMembers(study), study.getMaxMembers(), study.getNextSessionAt(),
                        study.getScheduleDescription(), study.getCompletedAt(), null, null,
                        memberships.findByStudyIdAndMemberMemberId(study.getId(), memberId)
                                .map(StudyMember::getEarnedXp).orElse(null))));
    }
}
