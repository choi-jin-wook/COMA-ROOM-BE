package com.coma.comaroom.event.dto;


import com.coma.comaroom.event.entity.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttendanceCheckRequestDto {
    private String eventName;              // 행사명
    private EventCategory eventCategory;   // 활동 종류 (ENUM)
    private Integer expirationTime;        // 유효시간 (분 단위)
}
