package com.coma.comaroom.member.controller;

import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.service.AuthService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth/")
public class AuthController {
    private final AuthService authService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> joinMember(@RequestBody RegisterMemberRequestDto registerMemberRequestDto) {
        authService.registerMember(registerMemberRequestDto);
        return Response.ok(registerMemberRequestDto, HttpStatus.CREATED).toResponseEntity();
    }

    // 리프레시 토큰
    @PostMapping("/refrash")
    public ResponseEntity<?> refreshMember(@RequestBody RegisterMemberRequestDto registerMemberRequestDto) {
        return Response.ok(HttpStatus.NOT_IMPLEMENTED).toResponseEntity();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login() {
        return Response.ok(HttpStatus.NOT_IMPLEMENTED).toResponseEntity();
    }
}
