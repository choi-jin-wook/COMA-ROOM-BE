package com.coma.comaroom.event.dto.response;

import com.coma.comaroom.event.entity.Event;

import java.time.LocalDateTime;

public record EventResponse(
        Long eventId,
        String title,
        LocalDateTime eventDate,
        Long rewardXp,
        String location,
        String category,
        String hostNickname
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getEventId(),
                event.getTitle(),
                event.getEventDate(),
                event.getRewardXp(),
                event.getLocation(),
                event.getEventCategory().name(),
                event.getHost().getName()
        );
    }
}