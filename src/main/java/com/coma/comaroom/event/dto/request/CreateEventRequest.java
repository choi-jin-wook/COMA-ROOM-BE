package com.coma.comaroom.event.dto.request;

import com.coma.comaroom.event.entity.EventCategory;
import lombok.*;

import java.time.LocalDateTime;


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
}