package com.coma.comaroom.event.dto;


import com.coma.comaroom.event.entity.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

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
}