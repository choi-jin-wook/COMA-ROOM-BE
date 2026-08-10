package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.member.entity.Member;
import lombok.Getter;
import lombok.Builder;
import java.util.List;

@Getter
@Builder
public class MainAttendanceResponseDto {
    // 1. 상단 출석 현황 카드
    private Long attendanceRate;      // 출석률 (75%)
    private Long attendanceCount;     // 출석 (6)
    private Long absenceCount;        // 결석 (1)
    private Long totalEventCount;     // 전체 (7)

    // 2. 중간 요약 지표
    private Long totalEarnedXp;       // 획득 XP (18)
    private Long attendanceRank;   // 출석 순위 (#6)

    // 3. 하단 출석 내역 (리스트)
    private List<AttendanceHistoryDto> attendanceHistory;

    public static MainAttendanceResponseDto of(
            Member member,
            Long rank,
            Long eventCount,
            Long attendanceCount,
            List<AttendanceHistoryDto> history
    ) {
        Long attendanceRate = 0L;
        if (eventCount > 0) {
            // 100을 먼저 곱해서 소수점 손실 없이 퍼센트 계산
            attendanceRate = (attendanceCount * 100) / eventCount;
        }

        return MainAttendanceResponseDto.builder()
                .totalEventCount(eventCount)
                .attendanceCount(attendanceCount)
                .absenceCount(eventCount - attendanceCount)
                .attendanceRate(attendanceRate)
                .totalEarnedXp(member.getXp())
                .attendanceRank(rank)
                .attendanceHistory(history)
                .build();
    }
}