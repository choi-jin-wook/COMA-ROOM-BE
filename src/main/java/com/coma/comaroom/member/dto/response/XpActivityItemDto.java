package com.coma.comaroom.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpActivityItemDto {
    private String activityType;   // REGULAR_MEETING, EVENT, STUDY, LAB, STAFF, APPROVAL
    private String title;          // 활동 제목 or 요청 사유
    private LocalDateTime date;    // 활동 날짜
    private Long xp;               // 획득 XP
    private String status;         // EventApproval일 경우: PENDING, APPROVED, REJECTED / 출석은 null
}
