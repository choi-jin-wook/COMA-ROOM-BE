package com.coma.comaroom.member.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class NoticeDto {
    private String title;      // "2026년 1학기 운영 계획 공지"
    private String content;
    private LocalDate date;       // "2026-01-10"
}
