package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.notice.entity.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class NoticeDto {
    private String title;      // "2026년 1학기 운영 계획 공지"
    private String content;
    private LocalDate date;       // "2026-01-10"

    public static NoticeDto from(Notice notice) {
        return NoticeDto.builder()
                .title(notice.getTitle())
                .content(notice.getContent())
                .date(notice.getCreatedAt().toLocalDate())
                .build();
    }
}
