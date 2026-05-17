package com.coma.comaroom.member.controller;

import com.coma.comaroom.event.dto.AskXpRequestDto;
import com.coma.comaroom.event.dto.AskXpResponseDto;
import com.coma.comaroom.event.dto.XpManagementMainResponseDto;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.member.dto.request.LeaderboardResponseDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.response.AttendanceMainResponse;
import com.coma.comaroom.member.dto.response.MainAttendanceResponseDto;
import com.coma.comaroom.member.dto.response.MainDashboardResponse;
import com.coma.comaroom.member.dto.response.ProfileResponseDto;
import com.coma.comaroom.member.dto.response.XpHistoryResponseDto;
import com.coma.comaroom.member.service.MemberService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;
    // 메인 페이지 1
    @GetMapping("/main")
    public ResponseEntity<Response<MainDashboardResponse>> getMainDashboard() {
        MainDashboardResponse mainDashboardResponse = memberService.getMainDashboard();
        return Response.ok(mainDashboardResponse, HttpStatus.OK).toResponseEntity();
    }

    // 리더보드
    @GetMapping("/leaderboard")
    public ResponseEntity<Response<LeaderboardResponseDto>> getLeaderboard() {
        LeaderboardResponseDto response = memberService.getLeaderboardData();
        return Response.ok(response, HttpStatus.OK).toResponseEntity();
    }

    @GetMapping("/main/attendance")
    public ResponseEntity<?> getMainAttendance() {
        MainAttendanceResponseDto mainAttendanceResponseDto = memberService.getMainAttendance();
        return Response.ok(mainAttendanceResponseDto, HttpStatus.OK).toResponseEntity();
    }



    // 프로필 (테스트 완료)
    @GetMapping("/profile")
    public ResponseEntity<?> getMemberProfile() {
        ProfileResponseDto profileResponseDto = memberService.getMemberProfile();
        return Response.ok(profileResponseDto, HttpStatus.OK).toResponseEntity();
    }

    // 사용자 xp 내역
    @GetMapping("/xp-history")
    public ResponseEntity<?> getMemberXpHistory(
            @RequestParam(defaultValue = "1") int page) {
        XpHistoryResponseDto response = memberService.getMemberXpHistory(page - 1);
        return Response.ok(response, HttpStatus.OK).toResponseEntity();
    }


    // 3. xp 메인페이지 (포스트맨 테스트 완료)
    @GetMapping("/main/xp")
    public ResponseEntity<Response<XpManagementMainResponseDto>> getXpMainPage(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "1") Long page) {
        XpManagementMainResponseDto xpManagementMainResponseDto = memberService.getXpManagementMainData(status, page - 1);
        return Response.ok(xpManagementMainResponseDto, HttpStatus.OK).toResponseEntity();
    }

    // xp지급 요청 (포스트맨 테스트 완료)
    @PostMapping("/ask-xp")
    public ResponseEntity<?> askXpProvision(@RequestBody AskXpRequestDto askXpRequestDto) {
        AskXpResponseDto xpPetitionResponseDto = memberService.askProvisionXp(askXpRequestDto);
        return Response.ok(xpPetitionResponseDto, HttpStatus.CREATED).toResponseEntity();
    }



}

