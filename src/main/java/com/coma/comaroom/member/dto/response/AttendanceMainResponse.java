package com.coma.comaroom.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceMainResponse {
    // 1. 상단 출석 현황 카드
    private int attendanceRate;      // 출석률 (75%)
    private int attendanceCount;     // 출석 (6)
    private int absenceCount;        // 결석 (1)
    private int totalEventCount;     // 전체 (7)

    // 2. 중간 요약 지표
    private int totalEarnedXp;       // 획득 XP (18)
    private String attendanceRank;   // 출석 순위 (#6)

    // 3. 하단 출석 내역 (리스트)
    private List<AttendanceHistoryDto> attendanceHistory;
}
