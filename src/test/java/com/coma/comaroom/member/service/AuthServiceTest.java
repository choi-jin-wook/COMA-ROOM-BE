package com.coma.comaroom.member.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.auth.jwt.JwtTokenProvider;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.dto.request.LoginRequestDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.response.LoginResponse;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.MemberStatus;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.notice.repository.NoticeRepository;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private NoticeRepository noticeRepository;
    @Mock private EventRepository eventRepository;
    @Mock private EventParticipateRepository eventParticipateRepository;
    @Mock private PasswordEncoder passwordEncoder;
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
        RegisterMemberRequestDto dto = RegisterMemberRequestDto.builder()
                .studentId("20210001")
                .name("테스터")
                .password("rawPassword")
                .major(Major.COMPUTER_INFO)
                .build();
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$encoded");
        when(memberRepository.saveAndFlush(any(Member.class))).thenReturn(member);

        assertThatNoException().isThrownBy(() -> authService.registerMember(dto));
        verify(passwordEncoder).encode("rawPassword");

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).saveAndFlush(captor.capture());

        Member saved = captor.getValue();
        assertThat(saved.getStudentId()).isEqualTo("20210001");
        assertThat(saved.getName()).isEqualTo("테스터");
        assertThat(saved.getPassword()).isEqualTo("$2a$10$encoded");
        assertThat(saved.getMajor()).isEqualTo(Major.COMPUTER_INFO);
        assertThat(saved.getXp()).isZero();
    }

    @Test
    @DisplayName("[보안] 회원가입 시 역할은 항상 USER로 고정된다")
    void registerMember_roleAlwaysUser() {
        RegisterMemberRequestDto dto = RegisterMemberRequestDto.builder()
                .studentId("20210002")
                .name("공격자")
                .password("pw")
                .major(Major.COMPUTER_INFO)
                .build();
        when(memberRepository.countByStudentIdIncludingWithdrawn("20210002")).thenReturn(0L);
        when(passwordEncoder.encode("pw")).thenReturn("$2a$10$encoded");

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        when(memberRepository.saveAndFlush(captor.capture())).thenReturn(member);

        authService.registerMember(dto);

        assertThat(captor.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("[보안] 중복 학번으로 회원가입 시 예외 발생")
    void registerMember_duplicateStudentId() {
        RegisterMemberRequestDto dto = RegisterMemberRequestDto.builder()
                .studentId("20210001")
                .name("테스터")
                .password("pw")
                .major(Major.COMPUTER_INFO)
                .build();
        when(memberRepository.countByStudentIdIncludingWithdrawn("20210001")).thenReturn(1L);

        assertThatThrownBy(() -> authService.registerMember(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthError.MEMBER_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("여러 회원을 한 번에 가입한다")
    void registerMembers_success() {
        RegisterMemberRequestDto first = RegisterMemberRequestDto.builder()
                .studentId("20210001")
                .name("member1")
                .password("password1")
                .major(Major.COMPUTER_INFO)
                .role(Role.USER)
                .build();
        RegisterMemberRequestDto second = RegisterMemberRequestDto.builder()
                .studentId("20210002")
                .name("member2")
                .password("password2")
                .major(Major.DATA_SCIENCE)
                .role(Role.USER)
                .build();
        when(passwordEncoder.encode("password1")).thenReturn("encoded1");
        when(passwordEncoder.encode("password2")).thenReturn("encoded2");

        authService.registerMembers(List.of(first, second));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Member>> captor = ArgumentCaptor.forClass(List.class);
        verify(memberRepository).saveAllAndFlush(captor.capture());
        assertThat(captor.getValue()).extracting(Member::getStudentId)
                .containsExactly("20210001", "20210002");
        assertThat(captor.getValue()).extracting(Member::getPassword)
                .containsExactly("encoded1", "encoded2");
    }

    @Test
    @DisplayName("한 요청에 같은 학번이 있으면 전체 가입을 거부한다")
    void registerMembers_duplicateStudentIdInRequest() {
        RegisterMemberRequestDto first = RegisterMemberRequestDto.builder()
                .studentId("20210001")
                .build();
        RegisterMemberRequestDto duplicate = RegisterMemberRequestDto.builder()
                .studentId("20210001")
                .build();

        assertThatThrownBy(() -> authService.registerMembers(List.of(first, duplicate)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthError.MEMBER_ALREADY_EXISTS.getMessage());

        verify(memberRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("회원 100명을 한 번에 저장한다")
    void registerMembers_oneHundredMembers() {
        List<RegisterMemberRequestDto> requests = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> RegisterMemberRequestDto.builder()
                        .studentId("%08d".formatted(i))
                        .name("member" + i)
                        .password("password" + i)
                        .major(Major.COMPUTER_INFO)
                        .role(Role.USER)
                        .build())
                .toList();
        when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

        authService.registerMembers(requests);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Member>> captor = ArgumentCaptor.forClass(List.class);
        verify(memberRepository).saveAllAndFlush(captor.capture());
        assertThat(captor.getValue()).hasSize(100);
        assertThat(captor.getValue().getFirst().getStudentId()).isEqualTo("00000001");
        assertThat(captor.getValue().getLast().getStudentId()).isEqualTo("00000100");
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
        when(jwtTokenProvider.createAccessToken(1L, "USER", "20210001")).thenReturn("access_token");
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

    // ─────────────────────────────────────────────
    // withdraw
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("회원 탈퇴 성공 - status가 WITHDRAWN으로 변경됨")
    void withdraw_success() {
        when(securityUtils.getCurrentMember()).thenReturn(member);

        authService.withdraw();

        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("회원 탈퇴 후 로그인 실패 - 탈퇴 회원은 조회되지 않음")
    void withdraw_afterWithdraw_loginFails() {
        // @SQLRestriction으로 인해 탈퇴 회원은 findByStudentId에서 반환되지 않음
        LoginRequestDto dto = mock(LoginRequestDto.class);
        when(dto.getStudentId()).thenReturn("20210001");
        when(memberRepository.findByStudentId("20210001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthError.LOGIN_FAIL.getMessage());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 인증되지 않은 사용자")
    void withdraw_unauthenticated() {
        when(securityUtils.getCurrentMember()).thenThrow(new BusinessException(AuthError.MEMBER_NOT_FOUND));

        assertThatThrownBy(() -> authService.withdraw())
                .isInstanceOf(BusinessException.class)
                .hasMessage(AuthError.MEMBER_NOT_FOUND.getMessage());
    }
}
