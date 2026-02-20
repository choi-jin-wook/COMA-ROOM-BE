package com.coma.comaroom.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaderboardResponseDto {
    private MyRankingDto myRanking;             // 나의 순위 정보
    private List<RankingItemDto> topThreeRankings;  // 상단 1, 2, 3위 리스트
    private List<RankingItemDto> allRankings;   // 전체 순위 리스트 (페이징 가능)
}