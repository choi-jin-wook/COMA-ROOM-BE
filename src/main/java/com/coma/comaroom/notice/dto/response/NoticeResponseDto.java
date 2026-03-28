package com.coma.comaroom.notice.dto.response;

import com.coma.comaroom.notice.entity.NoticePriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NoticeResponseDto {
    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private NoticePriority noticePriority;
    private LocalDateTime createdAt;
}
