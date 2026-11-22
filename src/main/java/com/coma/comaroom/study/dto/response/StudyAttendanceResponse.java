package com.coma.comaroom.study.dto.response;

import java.time.Instant;

/** 생성된 출석 세션의 ID, QR 식별자, 만료 시각을 반환한다. */
public record StudyAttendanceResponse(Long attendanceSessionId, String qrCodeId, Instant expiresAt) {}
