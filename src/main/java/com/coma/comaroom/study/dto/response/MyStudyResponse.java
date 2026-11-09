package com.coma.comaroom.study.dto.response;

/** 내 스터디 목록에 표시할 역할·일정·성과 정보를 담는다. */
public record MyStudyResponse(Long studyId, String studyName, String description, String myRole,
        Double progressPercent, long currentMembers,
        String scheduleDescription,
        Long totalSessions, Long attendedSessions) {}
