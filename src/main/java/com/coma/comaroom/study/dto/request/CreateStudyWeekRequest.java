package com.coma.comaroom.study.dto.request;

import jakarta.validation.constraints.*;

/** 생성할 주차 번호와 제목을 검증해 받는다. */
public record CreateStudyWeekRequest(@NotNull @Min(1) @Max(16) Integer weekNumber,
                                     @NotBlank @Size(max = 255) String title) {}
