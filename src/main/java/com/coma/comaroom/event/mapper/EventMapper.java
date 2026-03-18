package com.coma.comaroom.event.mapper;

import com.coma.comaroom.event.dto.request.EventRequest;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class EventMapper {
    public Event toEntity(EventRequest request, Member host) {
        return Event.builder()
                .title(request.title())
                .eventDate(request.eventDate())
                .rewardXp(request.rewardXp())
                .location(request.location())
                .eventCategory(request.eventCategory())
                .host(host)
                .eventParticipants(new ArrayList<>())
                .eventPosts(new ArrayList<>())
                .build();
    }
}