package com.coma.comaroom.notice.dto.response;

import com.coma.comaroom.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GetNoticeResponseDto {
    private Long totalNoticeCount;
    private Long pinnedNoticeCount;
    private Long openedNoticeCount;

    private List<NoticeResponseDto> pinnedNoticeList;
    private List<NoticeResponseDto> openedNoticeList;

    public static GetNoticeResponseDto of(List<Notice> pinnedNotices, Page<Notice> openedNoticePage) {
        // 1. 개별 엔티티를 NoticeResponseDto로 변환
        List<NoticeResponseDto> pinnedDtoList = pinnedNotices.stream()
                .map(NoticeResponseDto::from)
                .toList();

        List<NoticeResponseDto> openedDtoList = openedNoticePage.getContent().stream()
                .map(NoticeResponseDto::from)
                .toList();

        // 2. 카운트 계산
        long pinnedCount = pinnedNotices.size();
        long openedCount = openedNoticePage.getTotalElements(); // 전체 페이지의 총 개수

        // 3. 최종 DTO 생성
        return GetNoticeResponseDto.builder()
                .totalNoticeCount(pinnedCount + openedCount)
                .pinnedNoticeCount(pinnedCount)
                .openedNoticeCount(openedCount)
                .pinnedNoticeList(pinnedDtoList)
                .openedNoticeList(openedDtoList)
                .build();
    }
}
