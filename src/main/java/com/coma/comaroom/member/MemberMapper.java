package com.coma.comaroom.member;

import com.coma.comaroom.member.dto.request.LeaderboardResponseDto;
import com.coma.comaroom.member.dto.request.MyRankingDto;
import com.coma.comaroom.member.dto.request.RankingItemDto;
import com.coma.comaroom.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class MemberMapper {
    public MyRankingDto memberToMyRankingDto(Member currentMember, Long rank) {
        MyRankingDto myRankingDto = MyRankingDto.builder()
                .name(currentMember.getName())
                .major(currentMember.getMajor())
                .xp(currentMember.getXp())
                .rank(rank)
                .build();



        return myRankingDto;
    }

    public LeaderboardResponseDto MyRankingDtoAndMemberListToLeaderboardResponseDto(List<Member> memberList, MyRankingDto myRankingDto) {
// 1. 전체 리스트를 RankingItemDto 리스트로 변환
        List<RankingItemDto> allRankings = IntStream.range(0, memberList.size())
                .mapToObj(i -> {
                    Member m = memberList.get(i);
                    return RankingItemDto.builder()
                            .rank(i + 1)
                            .name(m.getName().length() > 1 ?
                                    m.getName().charAt(0) + "*" + m.getName().substring(m.getName().length() - 1) : m.getName())
                            .major(m.getMajor())
                            .xp(m.getXp().intValue())
                            // myRankingDto에 있는 이름이나 학번 등을 비교하여 본인 여부 확인
                            .isMe(m.getName().equals(myRankingDto.getName()))
                            .build();
                })
                .toList();

        // 2. 상위 3명 추출
        List<RankingItemDto> topThreeRankings = allRankings.stream()
                .limit(3)
                .toList();

        // 3. 최종 결과 조립
        return LeaderboardResponseDto.builder()
                .myRanking(myRankingDto)
                .topThreeRankings(topThreeRankings)
                .allRankings(allRankings)
                .build();
    }
}
