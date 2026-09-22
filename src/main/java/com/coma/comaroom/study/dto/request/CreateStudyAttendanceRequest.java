package com.coma.comaroom.study.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record CreateStudyAttendanceRequest(@NotNull @Min(1) Integer expirationTime) {}
