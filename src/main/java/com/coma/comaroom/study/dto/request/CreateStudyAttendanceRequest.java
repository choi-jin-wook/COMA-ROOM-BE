package com.coma.comaroom.study.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/** 기존 스터디 이벤트와 QR 유효 시간을 분 단위로 받는다. */
public record CreateStudyAttendanceRequest(@NotNull Long eventId, @NotNull @Min(1) Integer expirationTime) {}
