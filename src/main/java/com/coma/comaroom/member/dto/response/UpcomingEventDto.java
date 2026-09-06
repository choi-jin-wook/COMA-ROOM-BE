package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.event.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    public static UpcomingEventDto from(Event event) {
        LocalDateTime eventDate = event.getEventDate();

        return UpcomingEventDto.builder()
                .title(event.getTitle())
                .location(event.getLocation())
                .date(eventDate.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)))
                .dayOfWeek(eventDate.format(DateTimeFormatter.ofPattern("E", Locale.KOREAN)))
                .time(eventDate.format(DateTimeFormatter.ofPattern("a h시", Locale.KOREAN)))
                .build();
    }
}