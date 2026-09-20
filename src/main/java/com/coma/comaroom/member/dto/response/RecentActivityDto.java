package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentActivityDto {
    private String title;       // "정기모임 #7"
    private EventCategory eventCategory;    // "출석"
    private LocalDate date;        // "12월 30일"
    private Long rewardXp;      // 3 (포맷팅 없이 숫자값만 전달)

    public static RecentActivityDto from(EventParticipant participant) {
        Event event = participant.getEvent();

        return RecentActivityDto.builder()
                .title(event.getTitle())
                .eventCategory(event.getEventCategory())
                // BaseEntity의 생성일(LocalDateTime)에서 LocalDate만 추출
                .date(participant.getCreatedAt().toLocalDate())
                .rewardXp(event.getRewardXp())
                .build();
    }

    public static List<RecentActivityDto> listOf(List<EventParticipant> participants) {
        return participants.stream()
                .map(RecentActivityDto::from)
                .toList();
    }
}
