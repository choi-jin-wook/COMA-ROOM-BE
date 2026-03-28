package com.coma.comaroom.event.dto;


import com.coma.comaroom.event.entity.EventCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAttendanceCheckRequestDto {
    private Long eventId;
    private Integer expirationTime;
}
