package com.coma.comaroom.study.dto.response;

import java.util.List;

/** 스터디 정보와 1~16주차 계획의 요약 목록을 반환한다. */
public record StudyWeeksResponse(Long studyId, String studyName, String myRole, List<WeekSummary> weeks) {
    /** 계획이 없는 주차는 planId와 title이 null이다. */
    public record WeekSummary(int weekNumber, Long planId, String title) {}
}
