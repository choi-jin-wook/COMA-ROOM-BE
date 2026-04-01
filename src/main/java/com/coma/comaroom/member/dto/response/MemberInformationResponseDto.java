package com.coma.comaroom.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInformationResponseDto {
    private String name;
//    private String grade; // 일단 신경쓰지말기
    private String studentId; // 학번
    private String major; // 열거형 내부의 한글 이름의 학과 담기
    private Long xp; // 사용자의 xp
    private Long eventAttendance; // 열거형값 Event의 참여횟수
    private Long meetingAttendance; // 열거형값 정기회의의 참여횟수
}
