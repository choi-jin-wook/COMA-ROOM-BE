package com.coma.comaroom.study.dto.response;

import java.util.List;

/** 주차 상세 계획과 첨부 자료 목록을 반환한다. */
public record StudyWeekResponse(Long studyId, String studyName, String myRole, Long planId,
        int weekNumber, String title, String topic, String description, List<StudyMaterialResponse> materials) {}
