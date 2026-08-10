package com.coma.comaroom.vote.dto.response;

import com.coma.comaroom.vote.entity.VoteOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteOptionDetailResponseDto {
    private Long voteOptionId;
    private String content;
    private Long count;

    public static VoteOptionDetailResponseDto from(VoteOption option) {
        return VoteOptionDetailResponseDto.builder()
                .voteOptionId(option.getVoteOptionId())
                .content(option.getContent())
                .count(
                        Long.valueOf(Optional.ofNullable(option.getVoteResults())
                                .map(List::size)
                                .orElse(0))
                )
                .build();
    }
}
