package com.coma.comaroom.event.dto.request;

import com.coma.comaroom.event.entity.EventCategory;

import java.time.LocalDateTime;

public record EventRequest(
        String title,
        LocalDateTime eventDate,
        Long rewardXp,
        String location,
        EventCategory eventCategory
) {}