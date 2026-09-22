package com.coma.comaroom.study.dto.response;

import java.time.Instant;

public record StudyAttendanceResponse(Long attendanceSessionId, String qrCodeId, Instant expiresAt) {}
