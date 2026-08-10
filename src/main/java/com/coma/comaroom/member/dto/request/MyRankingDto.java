package com.coma.comaroom.member.dto.request;

import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyRankingDto {
    private Long rank;          // 나의 현재 순위
    private String name;           // 내 이름 (본인은 마스킹 없이 표시될 수 있음)
    private Major major;     // 내 학과
    private Long xp;            // 내 XP

    public static MyRankingDto of(Member currentMember, Long rank) {
        return MyRankingDto.builder()
                .name(currentMember.getName())
                .major(currentMember.getMajor())
                .xp(currentMember.getXp())
                .rank(rank)
                .build();
    }
}