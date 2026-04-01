package com.coma.comaroom.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpManagementPageResponseDto {
    private Long pending;
    private Long approved;
    private Long rejected;
    private List<EventApprovalResponseDto> eventApprovalResponseDtoList;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
