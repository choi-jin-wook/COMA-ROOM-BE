package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.entity.*;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 후보와 로그인 회원의 스터디 목록을 조회한다. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyService {
    private final StudyRepository studies;
    private final StudyManagerCandidateRepository candidates;
    private final StudyAccessService access;
    private final SecurityUtils security;

    /** 이름 또는 학번에 키워드가 포함된 스터디장 후보를 조회한다. */
    public Page<StudyManagerCandidate> managers(String keyword, int page, int size) {
        String search = keyword == null ? "" : keyword.strip();
        return candidates.findByNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(
                search, search, StudyAccessService.page(page, size, "memberId")).map(StudyManagerCandidate::from);
    }

    /** 요청 문자열을 검증한 뒤 형식이 지정된 조회 메서드로 전달한다. */
    public Page<MyStudyResponse> mine(String page, String size) {
        return mine(parseInteger(page), parseInteger(size));
    }

    /** 스터디장 또는 멤버로 참여한 스터디를 조회한다. */
    public Page<MyStudyResponse> mine(int page, int size) {
        Long memberId = security.getCurrentMember().getMemberId();
        return studies.findMine(memberId, StudyAccessService.page(page, size, "id"))
                .map(study -> new MyStudyResponse(study.getId(), study.getStudyName(), study.getDescription(),
                        access.isLeader(study, memberId) ? "LEADER" : "MEMBER", null,
                        access.currentMembers(study), study.getScheduleDescription(), null, null));
    }

    /** 숫자가 아닌 페이지 인자를 스터디 입력 오류로 처리한다. */
    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException(StudyError.INVALID_INPUT);
        }
    }
}
