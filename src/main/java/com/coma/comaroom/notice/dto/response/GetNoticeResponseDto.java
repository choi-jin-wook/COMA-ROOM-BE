package com.coma.comaroom.notice.dto.response;

import com.coma.comaroom.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
