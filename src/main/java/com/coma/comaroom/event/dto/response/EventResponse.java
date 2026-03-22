package com.coma.comaroom.event.dto.response;

import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;

import java.time.LocalDateTime;

public record EventResponse(
        Long eventId,
        String title,
        LocalDateTime eventDate,
        Long rewardXp,
        String location,
        EventCategory category,
        String hostname
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getEventId(),
                event.getTitle(),
                event.getEventDate(),
                event.getRewardXp(),
                event.getLocation(),
                event.getEventCategory(),
                event.getHost().getName()
        );
    }
}