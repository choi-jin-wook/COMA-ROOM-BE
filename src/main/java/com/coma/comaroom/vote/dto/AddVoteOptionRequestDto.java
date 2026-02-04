package com.coma.comaroom.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddVoteOptionRequestDto {
//    private Long voteId;
    private String content;
}
