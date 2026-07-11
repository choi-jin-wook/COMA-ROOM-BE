package com.coma.comaroom.member.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.dto.AskXpRequestDto;
import com.coma.comaroom.event.dto.AskXpResponseDto;
import com.coma.comaroom.event.dto.RecentActivityLogDto;
import com.coma.comaroom.event.dto.XpManagementMainResponseDto;
import com.coma.comaroom.event.entity.*;
import com.coma.comaroom.member.dto.response.XpHistoryResponseDto;
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
import java.util.ArrayList;
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
    @DisplayName("메인 대시보드 조회 성공 - 공지사항 없어도 정상 반환")
    void getMainDashboard_noticeNull() {
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(noticeRepository.findFirstByOrderByCreatedAtDesc()).thenReturn(Optional.empty());
        when(eventRepository.findFirstByEventDateAfterOrderByEventDateAsc(any())).thenReturn(Optional.empty());
        when(memberRepository.findRankByMember(member)).thenReturn(1L);
        when(eventParticipateRepository.countByParticipantMemberAndEvent_EventCategoryNot(member, EventCategory.EVENT)).thenReturn(0L);
        when(eventParticipateRepository.countByParticipantMemberAndEvent_EventCategory(member, EventCategory.EVENT)).thenReturn(0L);
        when(voteRepository.findFirstByVoteStatusOrderByCreatedAtDesc(VoteStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        MainDashboardResponse expected = mock(MainDashboardResponse.class);
        when(memberMapper.createMainDashboardResponse(any(), any(), isNull(), any(), any(), any(), any())).thenReturn(expected);

        MainDashboardResponse result = memberService.getMainDashboard();

        assertThat(result).isNotNull();
        verify(memberMapper).createMainDashboardResponse(any(), any(), isNull(), any(), any(), any(), any());
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
        when(securityUtils.getCurrentMember()).thenReturn(member);
        Page<EventApproval> page = new PageImpl<>(List.of());
        when(eventApprovalRepository.findByRequesterOrderByCreatedAtDesc(eq(member), any())).thenReturn(page);
        when(eventApprovalRepository.countByRequesterAndApprovalStatus(member, ApprovalStatus.APPROVED)).thenReturn(5L);
        when(eventApprovalRepository.countByRequesterAndApprovalStatus(member, ApprovalStatus.REJECTED)).thenReturn(2L);
        when(eventApprovalRepository.countByRequesterAndApprovalStatus(member, ApprovalStatus.PENDING)).thenReturn(3L);
        when(eventApprovalMapper.toRecentActivityLogDtos(anyList())).thenReturn(List.of());

        XpManagementMainResponseDto expected = mock(XpManagementMainResponseDto.class);
        when(xpManagementMapper.toMainDto(5L, 2L, 3L, List.of())).thenReturn(expected);

        XpManagementMainResponseDto result = memberService.getXpManagementMainData(null, 0L);

        assertThat(result).isNotNull();
        verify(eventApprovalRepository).findByRequesterOrderByCreatedAtDesc(eq(member), any());
    }

    @Test
    @DisplayName("XP 관리 데이터 조회 성공 - 상태 필터 있음")
    void getXpManagementMainData_withStatus() {
        when(securityUtils.getCurrentMember()).thenReturn(member);
        Page<EventApproval> page = new PageImpl<>(List.of());
        when(eventApprovalRepository.findByRequesterAndApprovalStatusOrderByCreatedAtDesc(eq(member), eq(ApprovalStatus.PENDING), any())).thenReturn(page);
        when(eventApprovalRepository.countByRequesterAndApprovalStatus(member, ApprovalStatus.APPROVED)).thenReturn(5L);
        when(eventApprovalRepository.countByRequesterAndApprovalStatus(member, ApprovalStatus.REJECTED)).thenReturn(2L);
        when(eventApprovalRepository.countByRequesterAndApprovalStatus(member, ApprovalStatus.PENDING)).thenReturn(3L);
        when(eventApprovalMapper.toRecentActivityLogDtos(anyList())).thenReturn(List.of());

        XpManagementMainResponseDto expected = mock(XpManagementMainResponseDto.class);
        when(xpManagementMapper.toMainDto(5L, 2L, 3L, List.of())).thenReturn(expected);

        XpManagementMainResponseDto result = memberService.getXpManagementMainData(ApprovalStatus.PENDING, 0L);

        assertThat(result).isNotNull();
        verify(eventApprovalRepository).findByRequesterAndApprovalStatusOrderByCreatedAtDesc(eq(member), eq(ApprovalStatus.PENDING), any());
    }

    // ─────────────────────────────────────────────
    // getMemberXpHistory
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("XP 내역 조회 성공 - 출석 + 행사 + 승인 요청 혼합")
    void getMemberXpHistory_success_mixed() {
        Member memberWith18Xp = Member.builder()
                .memberId(2L).studentId("20210002").name("홍길동")
                .password("$2a$10$enc").xp(18L).role(Role.USER).major(Major.COMPUTER_INFO)
                .build();
        when(securityUtils.getCurrentMember()).thenReturn(memberWith18Xp);

        Event regularEvent = mock(Event.class);
        when(regularEvent.getTitle()).thenReturn("정기모임 #7");
        when(regularEvent.getEventDate()).thenReturn(LocalDateTime.of(2025, 12, 30, 19, 0));
        when(regularEvent.getRewardXp()).thenReturn(3L);
        when(regularEvent.getEventCategory()).thenReturn(EventCategory.REGULAR_MEETING);

        Event festEvent = mock(Event.class);
        when(festEvent.getTitle()).thenReturn("나눔 우식 사업");
        when(festEvent.getEventDate()).thenReturn(LocalDateTime.of(2025, 12, 25, 14, 0));
        when(festEvent.getRewardXp()).thenReturn(5L);
        when(festEvent.getEventCategory()).thenReturn(EventCategory.EVENT);

        EventParticipant ep1 = mock(EventParticipant.class);
        when(ep1.getEvent()).thenReturn(regularEvent);

        EventParticipant ep2 = mock(EventParticipant.class);
        when(ep2.getEvent()).thenReturn(festEvent);

        when(eventParticipateRepository.findByParticipantMemberOrderByEventParticipantIdDesc(memberWith18Xp))
                .thenReturn(List.of(ep1, ep2));

        EventApproval approval = mock(EventApproval.class);
        when(approval.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(approval.getGrantedXp()).thenReturn(10L);
        when(approval.getReason()).thenReturn("특별 기여");
        when(approval.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 12, 20, 10, 0));

        when(eventApprovalRepository.findByRequesterOrderByCreatedAtDesc(memberWith18Xp))
                .thenReturn(List.of(approval));

        XpHistoryResponseDto result = memberService.getMemberXpHistory(0);

        assertThat(result).isNotNull();
        assertThat(result.getCurrentXp()).isEqualTo(18L);
        assertThat(result.getCurrentLevel()).isEqualTo(4);       // 18/5 + 1
        assertThat(result.getNextLevelXp()).isEqualTo(20L);      // 4 * 5
        assertThat(result.getXpInCurrentLevel()).isEqualTo(3L);  // 18 - 15
        assertThat(result.getAttendanceXp()).isEqualTo(3L);
        assertThat(result.getAttendanceCount()).isEqualTo(1L);
        assertThat(result.getEventXp()).isEqualTo(5L);
        assertThat(result.getEventCount()).isEqualTo(1L);
        assertThat(result.getApprovalXp()).isEqualTo(10L);
        assertThat(result.getApprovalCount()).isEqualTo(1L);
        assertThat(result.getTotalActivities()).isEqualTo(3);
        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getActivities()).hasSize(3);
        assertThat(result.getActivities().get(0).getTitle()).isEqualTo("정기모임 #7");
    }

    @Test
    @DisplayName("XP 내역 조회 성공 - 활동 없음")
    void getMemberXpHistory_empty() {
        Member emptyMember = Member.builder()
                .memberId(3L).studentId("20210003").name("빈유저")
                .password("$2a$10$enc").xp(0L).role(Role.USER).major(Major.COMPUTER_INFO)
                .build();
        when(securityUtils.getCurrentMember()).thenReturn(emptyMember);
        when(eventParticipateRepository.findByParticipantMemberOrderByEventParticipantIdDesc(emptyMember))
                .thenReturn(List.of());
        when(eventApprovalRepository.findByRequesterOrderByCreatedAtDesc(emptyMember))
                .thenReturn(List.of());

        XpHistoryResponseDto result = memberService.getMemberXpHistory(0);

        assertThat(result.getTotalActivities()).isEqualTo(0);
        assertThat(result.getActivities()).isEmpty();
        assertThat(result.getCurrentLevel()).isEqualTo(1);
        assertThat(result.getCurrentXp()).isEqualTo(0L);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("XP 내역 조회 성공 - 페이징 2페이지")
    void getMemberXpHistory_secondPage() {
        Member memberWithMany = Member.builder()
                .memberId(4L).studentId("20210004").name("활동왕")
                .password("$2a$10$enc").xp(50L).role(Role.USER).major(Major.COMPUTER_INFO)
                .build();
        when(securityUtils.getCurrentMember()).thenReturn(memberWithMany);

        List<EventParticipant> participants = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Event e = mock(Event.class);
            when(e.getTitle()).thenReturn("정기모임 #" + i);
            when(e.getEventDate()).thenReturn(LocalDateTime.of(2025, 12, i + 1, 19, 0));
            when(e.getRewardXp()).thenReturn(3L);
            when(e.getEventCategory()).thenReturn(EventCategory.REGULAR_MEETING);
            EventParticipant ep = mock(EventParticipant.class);
            when(ep.getEvent()).thenReturn(e);
            participants.add(ep);
        }

        when(eventParticipateRepository.findByParticipantMemberOrderByEventParticipantIdDesc(memberWithMany))
                .thenReturn(participants);
        when(eventApprovalRepository.findByRequesterOrderByCreatedAtDesc(memberWithMany))
                .thenReturn(List.of());

        XpHistoryResponseDto result = memberService.getMemberXpHistory(1);

        assertThat(result.getTotalActivities()).isEqualTo(11);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getCurrentPage()).isEqualTo(2);
        assertThat(result.getActivities()).hasSize(1);
    }

    @Test
    @DisplayName("XP 내역 조회 - PENDING 승인 요청은 approvalXp에 포함 안 됨")
    void getMemberXpHistory_pendingApprovalNotCounted() {
        when(securityUtils.getCurrentMember()).thenReturn(member);
        when(eventParticipateRepository.findByParticipantMemberOrderByEventParticipantIdDesc(member))
                .thenReturn(List.of());

        EventApproval pending = mock(EventApproval.class);
        when(pending.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);
        when(pending.getGrantedXp()).thenReturn(10L);
        when(pending.getReason()).thenReturn("미승인 요청");
        when(pending.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(eventApprovalRepository.findByRequesterOrderByCreatedAtDesc(member))
                .thenReturn(List.of(pending));

        XpHistoryResponseDto result = memberService.getMemberXpHistory(0);

        assertThat(result.getApprovalXp()).isEqualTo(0L);
        assertThat(result.getApprovalCount()).isEqualTo(0L);
        assertThat(result.getTotalActivities()).isEqualTo(1);
        assertThat(result.getActivities().get(0).getStatus()).isEqualTo("PENDING");
    }
}
