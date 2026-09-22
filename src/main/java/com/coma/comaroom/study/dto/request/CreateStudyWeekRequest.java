package com.coma.comaroom.study.dto.request;

import jakarta.validation.constraints.*;

public record CreateStudyWeekRequest(@NotNull @Min(1) @Max(16) Integer weekNumber,
                                     @NotBlank @Size(max = 255) String title) {}
