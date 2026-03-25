package com.coma.comaroom.member.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.dto.AskXpRequestDto;
import com.coma.comaroom.event.dto.AskXpResponseDto;
import com.coma.comaroom.event.dto.RecentActivityLogDto;
import com.coma.comaroom.event.dto.XpManagementMainResponseDto;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.mapper.EventApprovalMapper;
import com.coma.comaroom.event.repository.EventApprovalRepository;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.MemberMapper;
import com.coma.comaroom.member.XpManagementMapper;
import com.coma.comaroom.member.dto.request.LeaderboardResponseDto;
import com.coma.comaroom.member.dto.request.MyRankingDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.response.*;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.notice.entity.NoticePriority;
import com.coma.comaroom.notice.repository.NoticeRepository;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteStatus;
import com.coma.comaroom.vote.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private NoticeRepository noticeRepository;
    @Mock private EventRepository eventRepository;
    @Mock private EventParticipateRepository eventParticipateRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MemberMapper memberMapper;
    @Mock private SecurityUtils securityUtils;
    @Mock private VoteRepository voteRepository;
    @Mock private EventApprovalMapper eventApprovalMapper;
    @Mock private XpManagementMapper xpManagementMapper;
    @Mock private EventApprovalRepository eventApprovalRepository;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private Notice notice;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .studentId("20210001")
                .name("테스터")
                .password("$2a$10$encoded")
                .xp(500L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        notice = Notice.builder()
                .noticeId(1L)
                .title("공지사항")
                .content("내용")
                .pinned(false)
                .hidden(false)
                .noticePriority(NoticePriority.NORMAL)
                .author(member)
                .build();
    }

    // ─────────────────────────────────────────────
    // registerMember
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("회원가입 성공 - USER 역할 고정")
    void registerMember_success() {
        RegisterMemberRequestDto dto = mock(RegisterMemberRequestDto.class);
        when(dto.getStudentId()).thenReturn("20210001");
        when(dto.getName()).thenReturn("테스터");
        when(dto.getPassword()).thenReturn("rawPassword");
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$encoded");
        when(memberRepository.saveAndFlush(any(Member.class))).thenReturn(member);

        assertThatNoException().isThrownBy(() -> memberService.registerMember(dto));
        verify(memberRepository).saveAndFlush(argThat(m -> m.getRole() == Role.USER));
    }

    // ─────────────────────────────────────────────
    // askProvisionXp
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("XP 요청 성공")
    void askProvisionXp_success() {
        AskXpRequestDto dto = mock(AskXpRequestDto.class);
        when(dto.getProvisionReason()).thenReturn("스터디 참여");
        when(dto.getProvisionAmount()).thenReturn(50L);
        when(securityUtils.getCurrentMember()).thenReturn(member);

        EventApproval savedApproval = EventApproval.builder()
                .id(1L)
                .approvalStatus(ApprovalStatus.PENDING)
                .grantedXp(50L)
                .reason("스터디 참여")
                .requester(member)
                .build();
        when(eventApprovalRepository.save(any(EventApproval.class))).thenReturn(savedApproval);

        AskXpResponseDto result = memberService.askProvisionXp(dto);

        assertThat(result).isNotNull();
        verify(eventApprovalRepository).save(any(EventApproval.class));
    }

    // ─────────────────────────────────────────────
    // getLeaderboardData
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("리더보드 조회 성공")
    void getLeaderboardData_success() {
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(memberRepository.findRankByMember(member)).thenReturn(3L);
        when(memberRepository.findAllByOrderByXpDescMemberIdAsc(any())).thenReturn(List.of(member));

        MyRankingDto myRankingDto = mock(MyRankingDto.class);
        when(memberMapper.memberToMyRankingDto(member, 3L)).thenReturn(myRankingDto);

        LeaderboardResponseDto expected = mock(LeaderboardResponseDto.class);
        when(memberMapper.MyRankingDtoAndMemberListToLeaderboardResponseDto(anyList(), eq(myRankingDto))).thenReturn(expected);

        LeaderboardResponseDto result = memberService.getLeaderboardData();

        assertThat(result).isNotNull();
        verify(memberRepository).findAllByOrderByXpDescMemberIdAsc(any());
    }

    // ─────────────────────────────────────────────
    // getMainDashboard
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("메인 대시보드 조회 성공")
    void getMainDashboard_success() {
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(noticeRepository.findFirstByOrderByCreatedAtDesc()).thenReturn(Optional.of(notice));
        when(eventRepository.findFirstByEventDateAfterOrderByEventDateAsc(any())).thenReturn(Optional.empty());
        when(memberRepository.findRankByMember(member)).thenReturn(1L);
        when(eventParticipateRepository.countByParticipantMemberAndEvent_EventCategoryNot(member, EventCategory.EVENT)).thenReturn(5L);
        when(eventParticipateRepository.countByParticipantMemberAndEvent_EventCategory(member, EventCategory.EVENT)).thenReturn(2L);
        when(voteRepository.findFirstByVoteStatusOrderByCreatedAtDesc(VoteStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        MainDashboardResponse expected = mock(MainDashboardResponse.class);
        when(memberMapper.createMainDashboardResponse(any(), any(), any(), any(), any(), any(), any())).thenReturn(expected);

        MainDashboardResponse result = memberService.getMainDashboard();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("메인 대시보드 조회 실패 - 공지사항 없음")
    void getMainDashboard_noticeNotFound() {
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(noticeRepository.findFirstByOrderByCreatedAtDesc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMainDashboard())
                .isInstanceOf(BusinessException.class);
    }

    // ─────────────────────────────────────────────
    // getMemberProfile
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("멤버 프로필 조회 성공")
    void getMemberProfile_success() {
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(memberRepository.findRankByMember(member)).thenReturn(2L);
        when(eventParticipateRepository.countByParticipantMemberAndEvent_EventCategoryNot(member, EventCategory.EVENT)).thenReturn(3L);
        when(eventParticipateRepository.countByParticipantMemberAndEvent_EventCategory(member, EventCategory.EVENT)).thenReturn(1L);
        when(eventParticipateRepository.findTop5ByParticipantMemberOrderByEventParticipantIdDesc(member)).thenReturn(List.of());

        List<RecentActivityDto> recentActivityDtos = List.of();
        when(memberMapper.createRecentActivityDto(anyList())).thenReturn(recentActivityDtos);

        ProfileResponseDto expected = mock(ProfileResponseDto.class);
        when(memberMapper.createProfileResponseDto(eq(member), eq(2L), eq(3L), eq(1L), anyList())).thenReturn(expected);

        ProfileResponseDto result = memberService.getMemberProfile();

        assertThat(result).isNotNull();
    }

    // ─────────────────────────────────────────────
    // getMainAttendance
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("출석 메인 조회 성공")
    void getMainAttendance_success() {
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(memberRepository.findRankByMember(member)).thenReturn(1L);
        when(eventRepository.count()).thenReturn(10L);
        when(eventParticipateRepository.countByParticipantMember(member)).thenReturn(5L);
        when(eventRepository.findAllByOrderByEventDateDesc()).thenReturn(List.of());
        when(eventParticipateRepository.findAllEventIdsByMember(member)).thenReturn(List.of());

        MainAttendanceResponseDto expected = mock(MainAttendanceResponseDto.class);
        when(memberMapper.createMainAttendanceResponseDto(any(), any(), any(), any(), anyList())).thenReturn(expected);

        MainAttendanceResponseDto result = memberService.getMainAttendance();

        assertThat(result).isNotNull();
    }

    // ─────────────────────────────────────────────
    // getXpManagementMainData
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("XP 관리 데이터 조회 성공 - 상태 필터 없음")
    void getXpManagementMainData_withNullStatus() {
        Page<EventApproval> page = new PageImpl<>(List.of());
        when(eventApprovalRepository.findAllByOrderByCreatedAtDesc(any())).thenReturn(page);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED)).thenReturn(5L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED)).thenReturn(2L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING)).thenReturn(3L);
        when(eventApprovalMapper.toRecentActivityLogDtos(anyList())).thenReturn(List.of());

        XpManagementMainResponseDto expected = mock(XpManagementMainResponseDto.class);
        when(xpManagementMapper.toMainDto(5L, 2L, 3L, List.of())).thenReturn(expected);

        XpManagementMainResponseDto result = memberService.getXpManagementMainData(null, 0L);

        assertThat(result).isNotNull();
        verify(eventApprovalRepository).findAllByOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("XP 관리 데이터 조회 성공 - 상태 필터 있음")
    void getXpManagementMainData_withStatus() {
        Page<EventApproval> page = new PageImpl<>(List.of());
        when(eventApprovalRepository.findByApprovalStatusOrderByCreatedAtDesc(eq(ApprovalStatus.PENDING), any())).thenReturn(page);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED)).thenReturn(5L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED)).thenReturn(2L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING)).thenReturn(3L);
        when(eventApprovalMapper.toRecentActivityLogDtos(anyList())).thenReturn(List.of());

        XpManagementMainResponseDto expected = mock(XpManagementMainResponseDto.class);
        when(xpManagementMapper.toMainDto(5L, 2L, 3L, List.of())).thenReturn(expected);

        XpManagementMainResponseDto result = memberService.getXpManagementMainData(ApprovalStatus.PENDING, 0L);

        assertThat(result).isNotNull();
        verify(eventApprovalRepository).findByApprovalStatusOrderByCreatedAtDesc(eq(ApprovalStatus.PENDING), any());
    }
}
