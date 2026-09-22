package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.entity.Study;
import com.coma.comaroom.study.repository.StudyMemberRepository;
import com.coma.comaroom.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class StudyAccessService {
    private final StudyRepository studies;
    private final StudyMemberRepository members;

    public Study getStudy(Long studyId) {
        return studies.findById(studyId).orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
    }

    public Study lockStudy(Long studyId) {
        return studies.findForUpdate(studyId).orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
    }

    public boolean isLeader(Study study, Long memberId) {
        return study.getStudyManager() != null && study.getStudyManager().getMemberId().equals(memberId);
    }

    public String requireMember(Study study, Long memberId) {
        if (isLeader(study, memberId)) return "LEADER";
        if (members.existsByStudyIdAndMemberMemberId(study.getId(), memberId)) return "MEMBER";
        throw new BusinessException(StudyError.ACCESS_DENIED);
    }

    public void requireLeader(Study study, Long memberId) {
        if (!isLeader(study, memberId)) throw new BusinessException(StudyError.ACCESS_DENIED);
    }

    public long currentMembers(Study study) {
        long count = members.countMembers(study.getId());
        // 생성 시 소속이 등록되지 않았던 기존 스터디장도 인원에 포함한다.
        if (study.getStudyManager() != null && !members.existsByStudyIdAndMemberMemberId(
                study.getId(), study.getStudyManager().getMemberId())) count++;
        return count;
    }

    public static PageRequest page(int page, int size, String sortProperty) {
        if (page < 0 || size < 1 || size > 100) throw new BusinessException(StudyError.INVALID_INPUT);
        return PageRequest.of(page, size, Sort.by(sortProperty));
    }

    public static void validateWeek(int weekNumber) {
        if (weekNumber < 1 || weekNumber > 16) throw new BusinessException(StudyError.INVALID_INPUT);
    }
}
