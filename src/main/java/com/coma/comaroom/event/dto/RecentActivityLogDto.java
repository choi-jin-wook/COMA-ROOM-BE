package com.coma.comaroom.event.dto;


import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityLogDto {
    private Long id; // 이미 Long 유지
    private String userName;
    private String studentId;
    private String description;
    private String detail;
    private LocalDateTime dateTime;
    private Long grantedXp;
    private ApprovalStatus status;

    public static RecentActivityLogDto from(EventApproval approval) {
        Member requester = approval.getRequester();

        return RecentActivityLogDto.builder()
                .id(approval.getId())
                .userName(requester.getName())
                .studentId(requester.getStudentId())
                .description(approval.getReason())
                .dateTime(approval.getCreatedAt())
                .grantedXp(approval.getGrantedXp())
                .status(approval.getApprovalStatus())
                .build();
    }

    public static List<RecentActivityLogDto> listOf(List<EventApproval> approvals) {
        return approvals.stream()
                .map(RecentActivityLogDto::from)
                .toList();
    }
}