package com.coma.comaroom.event.dto;


import com.coma.comaroom.event.entity.EventCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAttendanceCheckRequestDto {
//    private String eventTitle;              // 행사명
//    private EventCategory eventCategory;   // 활동 종류 (ENUM)
//    private Integer expirationTime;        // 유효시간 (분 단위)
//    private String location;

    private Long eventId;
    private Integer expirationTime;
}
