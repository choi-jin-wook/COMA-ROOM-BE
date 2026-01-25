package com.coma.comaroom.member.service;

import com.coma.comaroom.member.dto.RequestRegisterMemberDto;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerMember(RequestRegisterMemberDto requestRegisterMemberDto) {

        Member member = Member.builder()
                .studentId(requestRegisterMemberDto.getStudentId())
                .name(requestRegisterMemberDto.getName())
                .role(Role.USER)
                .password(passwordEncoder.encode(requestRegisterMemberDto.getPassword()))
                .xp(0L)
                .build();


        memberRepository.save(member);
    }
}
