package com.coma.comaroom.member.dto.response;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class MainDashboardResponse {
    // 1. 상단 사용자 카드 정보
    private String semester;         // "2026년 1학기"
    private String userName;         // "최진욱"
    private Long currentXp;           // 18
    private Long remainingXp;         // 32

    // 2. 중앙 통계 지표 (요청하신 항목 제외)
    private int statAttendanceCount; // 출석 횟수: 6회
    private int statEventCount;      // 행사 참여: 2회

    // 3. 나의 순위 섹션
    private Long myRank;              // 12

    // 4. 각 섹션별 단일 데이터 (리스트 제거)
    private UpcomingEventDto upcomingEvent;
    private NoticeDto notice;
    private VoteDto votePoll;
}
