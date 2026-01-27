package com.coma.comaroom.member.controller;

import com.coma.comaroom.member.dto.RegisterMemberRequestDto;
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
    public ResponseEntity<?> joinMember(@RequestBody RegisterMemberRequestDto registerMemberRequestDto) {
        memberService.registerMember(registerMemberRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

//    @PostMapping("/api/auth/refrash")
//    public ResponseEntity<?> refreshMember(@RequestBody RegisterMemberRequestDto registerMemberRequestDto) {
//
//    }
}
