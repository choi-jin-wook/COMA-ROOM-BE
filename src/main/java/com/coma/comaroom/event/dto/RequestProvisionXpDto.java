package com.coma.comaroom.event.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestProvisionXpDto {
    private Long provisionAmount;
    private String provisionReason;

}
