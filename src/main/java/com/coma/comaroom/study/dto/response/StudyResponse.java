package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.study.entity.Study;

/** 생성된 스터디와 지정된 스터디장 정보를 반환한다. */
public record StudyResponse(
        Long studyId,
        String studyName,
        Long managerId,
        String managerName
) {
    /** 스터디장 참조가 없는 기존 데이터에도 응답을 생성한다. */
    public static StudyResponse from(Study study) {
        return new StudyResponse(
                study.getId(),
                study.getStudyName(),
                study.getStudyManager() != null ? study.getStudyManager().getMemberId() : null,
                study.getStudyManager() != null ? study.getStudyManager().getName() : "탈퇴한 회원"
        );
    }
}
