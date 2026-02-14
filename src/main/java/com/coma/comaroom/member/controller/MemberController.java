package com.coma.comaroom.member.controller;

import com.coma.comaroom.member.dto.RegisterMemberRequestDto;
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
        return Response.ok(registerMemberRequestDto, HttpStatus.CREATED).toResponseEntity();
    }

    @PostMapping("/api/auth/refrash")
    public ResponseEntity<?> refreshMember(@RequestBody RegisterMemberRequestDto registerMemberRequestDto) {
        return Response.ok(HttpStatus.NOT_IMPLEMENTED).toResponseEntity();
    }

    // 출석 메인 페이지
    @GetMapping("/api/attendance")
    public ResponseEntity<?> getAttendancePage() {
        return Response.ok(HttpStatus.NOT_IMPLEMENTED).toResponseEntity();
    }

    // 리더보드
    @GetMapping("/api/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        return Response.ok(HttpStatus.NOT_IMPLEMENTED).toResponseEntity();
    }

    // 프로필
    @GetMapping("/api/member/profile")
    public ResponseEntity<?> getMemberProfile() {
        return Response.ok(HttpStatus.NOT_IMPLEMENTED).toResponseEntity();
    }

    // 사용자 xp 내역
    @GetMapping("/api/member/xp-history")
    public ResponseEntity<?> getMemberXpHistory() {
        return Response.ok(HttpStatus.NOT_IMPLEMENTED).toResponseEntity();
    }

}
