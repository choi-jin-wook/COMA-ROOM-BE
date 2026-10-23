package com.coma.comaroom.member.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.auth.jwt.JwtTokenProvider;
import com.coma.comaroom.member.dto.request.LoginRequestDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.response.LoginResponse;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.MemberStatus;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import static com.coma.comaroom.member.AuthError.*;

@Service
@Transactional
@AllArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public void registerMember(RegisterMemberRequestDto registerMemberRequestDto) {
        // 탈퇴 회원은 @SQLRestriction으로 조회에서 제외되지만 student_id unique 제약은 남아있으므로
        // 탈퇴 회원까지 포함해 중복을 확인해야 DB 제약 위반(500)을 막을 수 있다
        if (memberRepository.countByStudentIdIncludingWithdrawn(registerMemberRequestDto.getStudentId()) > 0){
            throw new BusinessException(MEMBER_ALREADY_EXISTS);
        }

        Member member = registerMemberRequestDto.toEntity(
                passwordEncoder.encode(registerMemberRequestDto.getPassword()));

        memberRepository.saveAndFlush(member);
    }

    /**
     * 여러 회원을 하나의 트랜잭션에서 가입시킨다.
     * 요청 내부 또는 DB에 이미 존재하는 학번이 하나라도 있으면 전체 요청을 실패시킨다.
     */
    public void registerMembers(List<RegisterMemberRequestDto> registerMemberRequests) {
        Set<String> studentIds = new HashSet<>();
        for (RegisterMemberRequestDto request : registerMemberRequests) {
            if (!studentIds.add(request.getStudentId())
                    || memberRepository.countByStudentIdIncludingWithdrawn(request.getStudentId()) > 0) {
                throw new BusinessException(MEMBER_ALREADY_EXISTS);
            }
        }

        List<Member> members = registerMemberRequests.stream()
                .map(request -> request.toEntity(passwordEncoder.encode(request.getPassword())))
                .toList();

        memberRepository.saveAllAndFlush(members);
    }

    // 리프레시 토큰으로 액세스 토큰 재발급
    public String reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(INVALID_TOKEN);
        }
        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new BusinessException(INVALID_TOKEN);
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        return jwtTokenProvider.createAccessToken(member.getMemberId(), member.getRole().name(), member.getStudentId());
    }

    // 회원 탈퇴
    public void withdraw() {
        Member member = securityUtils.getCurrentMember();
        member.setStatus(MemberStatus.WITHDRAWN);
    }

    // 로그인
    public LoginResponse login(LoginRequestDto request) {
        // 1. 학번으로 회원 찾기
        Member member = memberRepository.findByStudentId(request.getStudentId()).orElseThrow(() -> new BusinessException(LOGIN_FAIL));


        // 2. 비밀번호 매칭 (BCrypt 등)
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(LOGIN_FAIL);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getRole().name(), member.getStudentId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());

        // 4. 응답 빌드 (Lombok @AllArgsConstructor 썼으니 이렇게)
        return new LoginResponse(
                accessToken,
                refreshToken,
                "로그인 성공",
                member.getRole()
        );
    }
}
