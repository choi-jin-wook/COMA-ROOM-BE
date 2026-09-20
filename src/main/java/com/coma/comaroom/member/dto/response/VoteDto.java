package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.vote.entity.Vote;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
public class VoteDto {
    private Long voteId;
    private String title;      // "다음 세미나 주제"
    private String description;// "2월 정기 세미나 주제를 선택해주세요."
    private Long remainingDays; // 14
    private Long rewardXp;      // 2

    public static VoteDto from(Vote vote) {
        return VoteDto.builder()
                .voteId(vote.getVoteId())
                .title(vote.getTitle())
//                .description(vote.getDescription())
                .remainingDays(ChronoUnit.DAYS.between(LocalDateTime.now(), vote.getDeadline()))
                .rewardXp(2L)
                .build();
    }
}