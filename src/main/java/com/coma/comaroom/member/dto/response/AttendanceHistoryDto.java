package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.event.entity.Event;
import lombok.Getter;
import lombok.Builder;

import java.util.Set;

@Getter
@Builder
public class AttendanceHistoryDto {
    private String title;            // 모임 명칭 (예: "정기모임 #8")
    private String scheduledDate;    // 예정 일시 (예: "2025-01-20 오후 7:00")
    private String location;         // 장소 (예: "N412")
    private String actualArrivalTime; // 실제 출석 시간 (예: "오후 6:55") - 결석 시 "-" 혹은 null

    private String status;           // 출석 상태 (예: "출석", "결석")
    private Long rewardXp;        // 획득 XP (예: 3) - 결석 시 null 혹은 0

    public static AttendanceHistoryDto of(Event event, Set<Long> attendedEventIds) {
        boolean isAttended = attendedEventIds.contains(event.getEventId());

        return AttendanceHistoryDto.builder()
                .title(event.getTitle())
                .status(isAttended ? "출석" : "결석")
                .scheduledDate(event.getEventDate().toString())
                .location(event.getLocation())
                .rewardXp(isAttended ? event.getRewardXp() : 0L)
                .build();
    }
}