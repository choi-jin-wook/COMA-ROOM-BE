package com.coma.comaroom.event.dto.request;

import com.coma.comaroom.event.entity.ApprovalStatus;

public record EventPostStatusRequest(
        ApprovalStatus approvalStatus
) {}