package com.coma.comaroom.member.controller;

import com.coma.comaroom.member.dto.request.LoginRequestDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.request.ReissueTokenRequestDto;
import com.coma.comaroom.member.dto.response.LoginResponse;
import com.coma.comaroom.member.dto.response.RegisterMemberResponseDto;
import com.coma.comaroom.member.dto.response.ReissueTokenResponseDto;
import com.coma.comaroom.member.service.AuthService;
import com.coma.comaroom.utils.Response;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    // 회원가입 (테스트 완료)
    @PostMapping("/register")
    public ResponseEntity<?> joinMember(@RequestBody @Valid RegisterMemberRequestDto registerMemberRequestDto) {
        authService.registerMember(registerMemberRequestDto);
        return Response.ok(RegisterMemberResponseDto.from(registerMemberRequestDto), HttpStatus.CREATED).toResponseEntity();
    }

    // 리프레시 토큰으로 액세스 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshMember(@RequestBody @Valid ReissueTokenRequestDto request) {
        String newAccessToken = authService.reissue(request.getRefreshToken());
        return Response.ok(new ReissueTokenResponseDto(newAccessToken), HttpStatus.OK).toResponseEntity();
    }

//    // 로그인 (테스트 완료)
//    @PostMapping("/login")
//    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
//        LoginResponse response = authService.login(loginRequestDto);
//        return Response.ok(response, HttpStatus.OK).toResponseEntity();
//    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdraw() {
        authService.withdraw();
        return Response.ok(null, HttpStatus.OK).toResponseEntity();
    }
}
