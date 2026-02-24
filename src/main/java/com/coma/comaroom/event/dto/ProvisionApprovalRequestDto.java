package com.coma.comaroom.event.dto;
import com.coma.comaroom.event.entity.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionApprovalRequestDto {

    // "APPROVED" 또는 "REJECTED"가 들어오는 필드
    private ApprovalStatus approvalStatus;

}
