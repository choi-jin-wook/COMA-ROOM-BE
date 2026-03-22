package com.coma.comaroom.member.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.auth.jwt.JwtTokenProvider;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.MemberMapper;
import com.coma.comaroom.member.dto.request.LoginRequestDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.response.LoginResponse;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.notice.repository.NoticeRepository;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.repository.VoteRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.coma.comaroom.member.AuthError.*;

@Service
@Transactional
@AllArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final EventRepository eventRepository;
    private final EventParticipateRepository eventParticipateRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;
    private final SecurityUtils securityUtils;
    private final VoteRepository voteRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public void registerMember(RegisterMemberRequestDto registerMemberRequestDto) {
        Member member = Member.builder()
                .studentId(registerMemberRequestDto.getStudentId())
                .name(registerMemberRequestDto.getName())
                .role(Role.ADMIN)
                .password(passwordEncoder.encode(registerMemberRequestDto.getPassword()))
                .xp(0L)
                .major(registerMemberRequestDto.getMajor())
                .build();

        memberRepository.saveAndFlush(member);
    }

    // 로그인
    public LoginResponse login(LoginRequestDto request) {
        // 1. 학번으로 회원 찾기
        Member member = memberRepository.findByStudentId(request.getStudentId()).orElseThrow(() -> new BusinessException(LOGIN_FAIL));


        // 2. 비밀번호 매칭 (BCrypt 등)
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(LOGIN_FAIL);
        }

        // 3. 토큰 생성 (지금은 placeholder – 실제로는 JWT 라이브러리 써서 만들어)
        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getRole().name());
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
