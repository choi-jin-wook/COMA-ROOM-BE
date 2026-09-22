package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.study.entity.StudyStatus;
import java.time.OffsetDateTime;

public record MyStudyResponse(Long studyId, String studyName, String description, String myRole,
        StudyStatus status, Double progressPercent, long currentMembers, Integer maxMembers,
        OffsetDateTime nextSessionAt, String scheduleDescription, OffsetDateTime completedAt,
        Long totalSessions, Long attendedSessions, Long earnedXp) {}
