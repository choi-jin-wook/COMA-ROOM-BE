package com.coma.comaroom.vote.component;

import com.coma.comaroom.vote.dto.response.VoteDetailResponseDto;
import com.coma.comaroom.vote.dto.response.VoteOptionDetailResponseDto;
import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class VoteMapper {
    public VoteDetailResponseDto toDetailDto(Vote vote) {
        return VoteDetailResponseDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
                .isMultiple(vote.isMultiVote())
                .status(vote.getVoteStatus())
                .options(
                        vote.getVoteOptions().stream()
                                .map(this::toOptionDto)
                                .toList()
                )
                .build();
    }

    private VoteOptionDetailResponseDto toOptionDto(VoteOption option) {
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
