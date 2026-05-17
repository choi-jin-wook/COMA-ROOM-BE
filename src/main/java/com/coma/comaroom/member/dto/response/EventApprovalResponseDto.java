package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.event.entity.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventApprovalResponseDto {
    private Long requestId;
    private String requester;
    private String studentId;
    private Long rewardXp;
    private String reason;
    private LocalDateTime localDateTime;
    private ApprovalStatus approvalStatus;
}
