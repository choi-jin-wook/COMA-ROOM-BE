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
public class XpHistoryResponseDto {

    // XP 요약
    private int currentLevel;          // 현재 레벨
    private Long currentXp;            // 총 XP
    private Long levelStartXp;         // 현재 레벨 시작 XP
    private Long nextLevelXp;          // 다음 레벨 목표 XP
    private Long xpInCurrentLevel;     // 현재 레벨에서 쌓은 XP

    // XP 분석
    private Long attendanceXp;         // 출석 XP (정기회의, 랩실, 스터디, 스태프)
    private Long attendanceCount;      // 출석 횟수
    private Long eventXp;              // 행사 참석 XP
    private Long eventCount;           // 행사 참석 횟수
    private Long approvalXp;           // 기타(수동 지급) XP
    private Long approvalCount;        // 기타 횟수

    // 전체 활동 목록 (페이징)
    private long totalActivities;      // 전체 활동 수
    private int currentPage;           // 현재 페이지 (1-based)
    private int totalPages;            // 전체 페이지 수
    private List<XpActivityItemDto> activities;
}
