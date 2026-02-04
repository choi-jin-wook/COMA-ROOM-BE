package com.coma.comaroom.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpProvisionRequestDto {

    private String studentId;

    // 부여된 XP 수량
    private Long provisionAmount;

    // 지급 사유
    private String provisionReason;

}