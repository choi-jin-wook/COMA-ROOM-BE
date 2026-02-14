package com.coma.comaroom.notice.dto.response;

import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.notice.entity.NoticePriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CreateNoticeResponseDto {

    private Long noticeId;

    private String title;
    private String content;

    private boolean pinned;
    private boolean hidden;

    private Long authorId;
    private String authorName;

    private NoticePriority noticePriority;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}