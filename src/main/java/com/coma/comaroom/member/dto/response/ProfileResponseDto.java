package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDto {
    // 상단 프로필 카드
    private String name;           // "서진호"
    private Major major;          // "컴퓨터정보공학과"
    private String studentId;      // "202121180"
    private Long ranking;          // 12
    private Long currentXp;        // 18
    private LocalDate joinedDate;     // "2024년 3월 1일"
    private Role memberStatus;   // "일반 회원"

    // 핵심 요약 (4개 카드)
    private Long attendanceCount;  // 6
    private Long eventCount;       // 2

    private List<RecentActivityDto> recentActivities;
}
