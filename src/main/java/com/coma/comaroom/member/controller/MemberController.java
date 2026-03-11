package com.coma.comaroom.member.controller;

import com.coma.comaroom.member.dto.request.LeaderboardResponseDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.response.AttendanceMainResponse;
import com.coma.comaroom.member.dto.response.MainAttendanceResponseDto;
import com.coma.comaroom.member.dto.response.MainDashboardResponse;
import com.coma.comaroom.member.dto.response.ProfileResponseDto;
import com.coma.comaroom.member.service.MemberService;
import com.coma.comaroom.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 회원가입
    @PostMapping("/api/auth/register")
    public ResponseEntity<?> joinMember(@RequestBody RegisterMemberRequestDto registerMemberRequestDto) {
        memberService.registerMember(registerMemberRequestDto);
        return Response.ok(registerMemberRequestDto, HttpStatus.CREATED).toResponseEntity();
    }

    // 리프레시 토큰
    @PostMapping("/api/auth/refrash")
    public ResponseEntity<?> refreshMember(@RequestBody RegisterMemberRequestDto registerMemberRequestDto) {
        return Response.ok(HttpStatus.NOT_IMPLEMENTED).toResponseEntity();
    }


    // 프로필 (테스트 완료)
    @GetMapping("/api/member/profile")
    public ResponseEntity<?> getMemberProfile() {
        ProfileResponseDto profileResponseDto = memberService.getMemberProfile();
        return Response.ok(profileResponseDto, HttpStatus.OK).toResponseEntity();
    }

    // 사용자 xp 내역
    @GetMapping("/api/member/xp-history")
    public ResponseEntity<?> getMemberXpHistory() {
        return Response.ok(HttpStatus.NOT_IMPLEMENTED).toResponseEntity();
    }

    // 메인 페이지 1
    @GetMapping("/api/member/main")
    public ResponseEntity<Response<MainDashboardResponse>> getMainDashboard() {
        MainDashboardResponse mainDashboardResponse = memberService.getMainDashboard();
        return Response.ok(mainDashboardResponse, HttpStatus.OK).toResponseEntity();
    }

    // 메인 페이지 출석
    @GetMapping("/api/member/main/attendance")
    public ResponseEntity<?> getMainAttendance() {
        MainAttendanceResponseDto mainAttendanceResponseDto = memberService.getMainAttendance();
        return Response.ok(mainAttendanceResponseDto, HttpStatus.OK).toResponseEntity();
    }


    // 리더보드
    @GetMapping("/api/member/leaderboard")
    public ResponseEntity<Response<LeaderboardResponseDto>> getLeaderboard() {
        LeaderboardResponseDto response = memberService.getLeaderboardData();
        return Response.ok(response, HttpStatus.OK).toResponseEntity();
    }
}

