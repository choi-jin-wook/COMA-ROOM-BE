package com.coma.comaroom.member.dto.response;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class AttendanceHistoryDto {
    private String title;            // 모임 명칭 (예: "정기모임 #8")
    private String scheduledDate;    // 예정 일시 (예: "2025-01-20 오후 7:00")
    private String location;         // 장소 (예: "N412")
    private String actualArrivalTime; // 실제 출석 시간 (예: "오후 6:55") - 결석 시 "-" 혹은 null

    private String status;           // 출석 상태 (예: "출석", "결석")
    private Integer rewardXp;        // 획득 XP (예: 3) - 결석 시 null 혹은 0
}