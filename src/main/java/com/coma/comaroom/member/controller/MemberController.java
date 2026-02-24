package com.coma.comaroom.member.controller;

import com.coma.comaroom.member.dto.request.LeaderboardResponseDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.response.MainDashboardResponse;
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

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> joinMember(@RequestBody RegisterMemberRequestDto registerMemberRequestDto) {
        memberService.registerMember(registerMemberRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

//    @PostMapping("/api/auth/refrash")
//    public ResponseEntity<?> refreshMember(@RequestBody RegisterMemberRequestDto registerMemberRequestDto) {
//
//    }


    // 메인 페이지 1
    @GetMapping
    public ResponseEntity<Response<MainDashboardResponse>> getMainDashboard() {
        MainDashboardResponse mainDashboardResponse = memberService.getMainDashboard();
        return Response.ok(mainDashboardResponse, HttpStatus.OK).toResponseEntity();
    }

    // 리더보드
    @GetMapping("/v1/leaderboard")
    public ResponseEntity<Response<LeaderboardResponseDto>> getLeaderboard() {
        // userMember.getId() 등을 전달하여 '나의 순위'와 '전체 리스트'를 함께 조회
        LeaderboardResponseDto response = memberService.getLeaderboardData();
        return Response.ok(response, HttpStatus.OK).toResponseEntity();
    }

    // 메인 페이지 출석


}
