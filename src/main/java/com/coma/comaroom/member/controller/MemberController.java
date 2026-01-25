package com.coma.comaroom.member.controller;

import com.coma.comaroom.member.dto.RequestRegisterMemberDto;
import com.coma.comaroom.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> joinMember(@RequestBody RequestRegisterMemberDto requestRegisterMemberDto) {
        memberService.registerMember(requestRegisterMemberDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
