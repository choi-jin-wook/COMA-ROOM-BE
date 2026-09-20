package com.coma.comaroom.event.dto.request;

import com.coma.comaroom.event.entity.ApprovalStatus;
import jakarta.validation.constraints.NotNull;

public record EventPostStatusRequest(
        @NotNull
        ApprovalStatus approvalStatus
) {}