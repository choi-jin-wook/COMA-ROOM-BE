package com.coma.comaroom.member.dto.request;

import com.coma.comaroom.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.IntStream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaderboardResponseDto {
    private MyRankingDto myRanking;             // 나의 순위 정보
    private List<RankingItemDto> topThreeRankings;  // 상단 1, 2, 3위 리스트
    private List<RankingItemDto> allRankings;   // 전체 순위 리스트 (페이징 가능)

    public static LeaderboardResponseDto of(List<Member> memberList, MyRankingDto myRanking) {
        // 1. 전체 리스트를 RankingItemDto 리스트로 변환
        List<RankingItemDto> allRankings = IntStream.range(0, memberList.size())
                .mapToObj(i -> {
                    Member member = memberList.get(i);
                    // myRanking에 있는 이름을 비교하여 본인 여부 확인
                    boolean isMe = member.getName().equals(myRanking.getName());
                    return RankingItemDto.of(member, i + 1, isMe);
                })
                .toList();

        // 2. 상위 3명 추출
        List<RankingItemDto> topThreeRankings = allRankings.stream()
                .limit(3)
                .toList();

        // 3. 최종 결과 조립
        return LeaderboardResponseDto.builder()
                .myRanking(myRanking)
                .topThreeRankings(topThreeRankings)
                .allRankings(allRankings)
                .build();
    }
}