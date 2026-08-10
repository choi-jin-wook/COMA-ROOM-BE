package com.coma.comaroom.event.dto.request;

import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.member.entity.Member;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Getter
@Setter // setter 사용 가능
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {
    private String title;
    private LocalDateTime eventDate;
    private String location;
    private EventCategory eventCategory;
    private Long rewardXp;

    public Event toEntity(Member host) {
        return Event.builder()
                .title(title)
                .eventDate(eventDate)
                .rewardXp(rewardXp)
                .location(location)
                .eventCategory(eventCategory)
                .host(host)
                .eventParticipants(new ArrayList<>())
                .eventPosts(new ArrayList<>())
                .build();
    }
}