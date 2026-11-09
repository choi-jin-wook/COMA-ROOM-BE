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

/** 스터디 조회, 역할 확인 및 공통 입력 범위 검사를 담당한다. */
@Service
@RequiredArgsConstructor
public class StudyAccessService {
    private final StudyRepository studies;
    private final StudyMemberRepository members;

    /** 존재하지 않는 스터디를 도메인 오류로 변환한다. */
    public Study getStudy(Long studyId) {
        return studies.findById(studyId).orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
    }

    /** 참여 신청 등 동시 수정이 가능한 작업에서 스터디 행을 잠근다. */
    public Study lockStudy(Long studyId) {
        return studies.findForUpdate(studyId).orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
    }

    /** 현재 지정된 스터디장과 회원 ID를 비교한다. */
    public boolean isLeader(Study study, Long memberId) {
        return study.getStudyManager() != null && study.getStudyManager().getMemberId().equals(memberId);
    }

    /** 접근 권한을 확인하고 응답에 사용할 역할을 반환한다. */
    public String requireMember(Study study, Long memberId) {
        if (isLeader(study, memberId)) return "LEADER";
        if (members.existsByStudyIdAndMemberMemberId(study.getId(), memberId)) return "MEMBER";
        throw new BusinessException(StudyError.ACCESS_DENIED);
    }

    /** 쓰기 작업을 스터디장에게만 허용한다. */
    public void requireLeader(Study study, Long memberId) {
        if (!isLeader(study, memberId)) throw new BusinessException(StudyError.ACCESS_DENIED);
    }

    /** 스터디장이 멤버 테이블에 없더라도 정원 계산에는 포함한다. */
    public long currentMembers(Study study) {
        long count = members.countMembers(study.getId());
        // 생성 시 소속이 등록되지 않았던 기존 스터디장도 인원에 포함한다.
        if (study.getStudyManager() != null && !members.existsByStudyIdAndMemberMemberId(
                study.getId(), study.getStudyManager().getMemberId())) count++;
        return count;
    }

    /** 페이지 크기를 1~100으로 제한하고 정렬 조건을 만든다. */
    public static PageRequest page(int page, int size, String sortProperty) {
        if (page < 0 || size < 1 || size > 100) throw new BusinessException(StudyError.INVALID_INPUT);
        return PageRequest.of(page, size, Sort.by(sortProperty));
    }

    /** 스터디 주차는 1~16주차만 허용한다. */
    public static void validateWeek(int weekNumber) {
        if (weekNumber < 1 || weekNumber > 16) throw new BusinessException(StudyError.INVALID_INPUT);
    }
}
