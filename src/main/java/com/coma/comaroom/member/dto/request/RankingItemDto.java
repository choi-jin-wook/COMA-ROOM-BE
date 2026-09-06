package com.coma.comaroom.member.dto.request;

import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RankingItemDto {
    private Integer rank;          // 순위 (1, 2, 3...)
    private String name;          // 이름 (익명 처리된 이름: 김*현)
    private Major major;   // 학과 (컴퓨터정보공학부 등)
    private Integer xp;           // 보유 XP
    private Boolean isMe;         // 본인 여부 (UI에서 '나' 태그 표시용)

    public static RankingItemDto of(Member member, int rank, boolean isMe) {
        return RankingItemDto.builder()
                .rank(rank)
                .name(maskName(member.getName()))
                .major(member.getMajor())
                .xp(member.getXp().intValue())
                .isMe(isMe)
                .build();
    }

    // 김진욱 -> 김*욱 (한 글자 이름은 그대로 노출)
    private static String maskName(String name) {
        return name.length() > 1
                ? name.charAt(0) + "*" + name.substring(name.length() - 1)
                : name;
    }
}