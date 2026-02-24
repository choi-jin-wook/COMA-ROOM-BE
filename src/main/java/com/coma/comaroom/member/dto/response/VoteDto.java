package com.coma.comaroom.member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoteDto {
    private Long voteId;
    private String title;      // "다음 세미나 주제"
    private String description;// "2월 정기 세미나 주제를 선택해주세요."
    private int remainingDays; // 14
    private int rewardXp;      // 2
}