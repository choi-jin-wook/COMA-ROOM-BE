package com.coma.comaroom.event.mapper;

import com.coma.comaroom.event.dto.RecentActivityLogDto;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventApprovalMapper {

    public List<RecentActivityLogDto> toRecentActivityLogDtos(List<EventApproval> approvals) {
        return approvals.stream()
                .map(approval -> {
                    // Requester가 null일 경우를 대비한 방어 코드
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
                })
                .toList();
    }
}
