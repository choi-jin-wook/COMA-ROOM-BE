package com.coma.comaroom.event.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAttendanceRequestDto {
    private String qrCodeId;
}
