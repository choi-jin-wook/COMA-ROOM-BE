package com.coma.comaroom.member;

import com.coma.comaroom.event.dto.RecentActivityLogDto;
import com.coma.comaroom.event.dto.XpManagementMainResponseDto;
import com.coma.comaroom.event.entity.EventApproval;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XpManagementMapper {

    public List<RecentActivityLogDto> toRecentActivityLogDtos(List<EventApproval> approvals) {
        return approvals.stream()
                .map(this::toRecentActivityLogDto)
                .toList();
    }

    private RecentActivityLogDto toRecentActivityLogDto(EventApproval approval) {
        return RecentActivityLogDto.builder()
                .id(approval.getId())
                .userName(approval.getRequester().getName())
                .studentId(approval.getRequester().getStudentId())
                .description(approval.getReason())
                .dateTime(approval.getCreatedAt())
                .grantedXp(approval.getGrantedXp())
                .status(approval.getApprovalStatus())
                .build();
    }

    public XpManagementMainResponseDto toMainDto(
            long approvedCount,
            long rejectedCount,
            long pendingCount,
            List<RecentActivityLogDto> recentActivityLogs
    ) {
        return XpManagementMainResponseDto.builder()
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .pendingCount(pendingCount)
                .recentActivityLogs(recentActivityLogs)
                .build();
    }
}
