package com.coma.comaroom.vote.dto.request;

import com.coma.comaroom.vote.entity.VoteOption;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVoteOptionRequestDto {
    @NotBlank(message = "선택지 내용은 필수 입력 항목입니다.")
    private String content;

    public VoteOption toEntity() {
        return VoteOption.builder()
                .content(content)
                .build();
    }
}