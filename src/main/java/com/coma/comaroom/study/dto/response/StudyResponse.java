package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.study.entity.Study;

public record StudyResponse(
        Long studyId,
        String studyName,
        String managerName
) {
    public static StudyResponse from(Study study) {
        return new StudyResponse(
                study.getId(),
                study.getStudyName(),
                study.getStudyManager() != null ? study.getStudyManager().getName() : "탈퇴한 회원"
        );
    }
}
