package com.coma.comaroom.event.mapper;

import com.coma.comaroom.event.dto.request.CreateEventRequest;
import com.coma.comaroom.event.dto.request.EventRequest;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class EventMapper {
    public Event toEntity(CreateEventRequest request, Member host) {
        return Event.builder()
                .title(request.getTitle())
                .eventDate(request.getEventDate())
                .rewardXp(request.getRewardXp())
                .location(request.getLocation())
                .eventCategory(request.getEventCategory())
                .host(host)
                .eventParticipants(new ArrayList<>())
                .eventPosts(new ArrayList<>())
                .build();
    }
}