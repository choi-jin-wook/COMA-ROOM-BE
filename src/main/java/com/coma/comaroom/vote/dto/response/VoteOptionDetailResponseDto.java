package com.coma.comaroom.vote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteOptionDetailResponseDto {
    private Long voteOptionId;
    private String content;
}
