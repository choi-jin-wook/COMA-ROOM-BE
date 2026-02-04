package com.coma.comaroom.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpManagementMainResponseDto {
    // 카운트 지표들을 Long으로 변경 (DB count 쿼리 결과값 대응)
    private Long pendingCount;
    private Long approvedCount;
    private Long rejectedCount;

    private List<RecentActivityLogDto> recentActivityLogs;
}