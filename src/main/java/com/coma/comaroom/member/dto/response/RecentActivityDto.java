package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.event.entity.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentActivityDto {
    private String title;       // "정기모임 #7"
    private EventCategory eventCategory;    // "출석"
    private LocalDate date;        // "12월 30일"
    private Long rewardXp;      // 3 (포맷팅 없이 숫자값만 전달)
}
