package com.coma.comaroom.member.dto.request;

import com.coma.comaroom.member.entity.Major;
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
}