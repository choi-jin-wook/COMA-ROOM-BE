package com.coma.comaroom.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpcomingEventDto {
    private String title;      // "정기모임 8회"
    private String date;       // "3월 3일"
    private String dayOfWeek;  // "화"
    private String time;       // "오후 7시"
    private String location;   // "NHN2"
    private int rewardXp;      // 3
}