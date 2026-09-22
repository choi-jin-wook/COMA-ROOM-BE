package com.coma.comaroom.study.dto.response;

import java.util.List;

public record StudyWeeksResponse(Long studyId, String studyName, String myRole, List<WeekSummary> weeks) {
    public record WeekSummary(int weekNumber, Long planId, String title) {}
}
