package com.coma.comaroom.vote.dto.response;


import com.coma.comaroom.vote.dto.request.CreateNewVoteRequestDto;
import com.coma.comaroom.vote.entity.VoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteDetailResponseDto {
    private Long voteId;
    private String title;
    private VoteStatus status;
    private Boolean isMultiple;
    private List<VoteOptionDetailResponseDto> options;
    private Boolean voted;
}
