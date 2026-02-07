package com.coma.comaroom.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestProvisionXpDto {
    private Long provisionAmount;
    private String provisionReason;

}
