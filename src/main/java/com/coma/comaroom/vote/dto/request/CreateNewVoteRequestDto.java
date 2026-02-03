package com.coma.comaroom.vote.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewVoteRequestDto {
    @NotBlank(message = "투표 제목은 필수 입력 항목입니다.")
    private String title;

    @NotNull(message = "투표 타입(SINGLE, MULTI)을 선택해주세요.")
    private Boolean isMultiple;

    @NotEmpty(message = "최소 하나 이상의 투표 선택지가 필요합니다.")
    private List<CreateVoteOptionRequestDto> options;

    @NotEmpty(message = "마감일은 필수z입니다")
    private LocalDateTime deadline;
}
