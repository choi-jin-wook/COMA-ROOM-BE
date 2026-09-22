package com.coma.comaroom.study.dto.response;

import java.util.List;

public record StudyWeekResponse(Long studyId, String studyName, String myRole, Long planId,
        int weekNumber, String title, String topic, String description, List<StudyMaterialResponse> materials) {}
