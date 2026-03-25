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
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.notice.repository.NoticeRepository;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private NoticeRepository noticeRepository;
    @Mock private EventRepository eventRepository;
    @Mock private EventParticipateRepository eventParticipateRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MemberMapper memberMapper;
    @Mock private SecurityUtils securityUtils;
    @Mock private VoteRepository voteRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .studentId("20210001")
                .name("테스터")
                .password("$2a$10$encodedPassword")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();
    }

    // ─────────────────────────────────────────────
    // registerMember
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("회원가입 성공")
    void registerMember_success() {
        RegisterMemberRequestDto dto = mock(RegisterMemberRequestDto.class);
        when(dto.getStudentId()).thenReturn("20210001");
        when(dto.getName()).thenReturn("테스터");
        when(dto.getPassword()).thenReturn("rawPassword");
        when(dto.getRole()).thenReturn(Role.USER);
        when(dto.getMajor()).thenReturn(Major.COMPUTER_INFO);
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$encoded");
        when(memberRepository.saveAndFlush(any(Member.class))).thenReturn(member);

        assertThatNoException().isThrownBy(() -> authService.registerMember(dto));
        verify(memberRepository).saveAndFlush(any(Member.class));
        verify(passwordEncoder).encode("rawPassword");
    }

    // ─────────────────────────────────────────────
    // login
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        LoginRequestDto dto = mock(LoginRequestDto.class);
        when(dto.getStudentId()).thenReturn("20210001");
        when(dto.getPassword()).thenReturn("rawPassword");
        when(memberRepository.findByStudentId("20210001")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("rawPassword", member.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(1L, "USER")).thenReturn("access_token");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh_token");

        LoginResponse result = authService.login(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access_token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(result.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("로그인 실패 - 학번에 해당하는 회원 없음")
    void login_memberNotFound() {
        LoginRequestDto dto = mock(LoginRequestDto.class);
        when(dto.getStudentId()).thenReturn("99999999");
        when(memberRepository.findByStudentId("99999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthError.LOGIN_FAIL.getMessage());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_wrongPassword() {
        LoginRequestDto dto = mock(LoginRequestDto.class);
        when(dto.getStudentId()).thenReturn("20210001");
        when(dto.getPassword()).thenReturn("wrongPassword");
        when(memberRepository.findByStudentId("20210001")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongPassword", member.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthError.LOGIN_FAIL.getMessage());
    }
}
