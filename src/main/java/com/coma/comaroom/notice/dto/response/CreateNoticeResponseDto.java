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

    public static CreateNoticeResponseDto from(Notice notice) {
        return CreateNoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .pinned(notice.isPinned())
                .hidden(notice.isHidden())
                .authorId(notice.getAuthor().getMemberId())
                .authorName(notice.getAuthor().getName())
                .noticePriority(notice.getNoticePriority())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}