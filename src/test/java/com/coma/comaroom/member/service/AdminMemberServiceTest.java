package com.coma.comaroom.member.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.dto.ProvisionApprovalRequestDto;
import com.coma.comaroom.event.dto.XpProvisionRequestDto;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.event.mapper.EventApprovalMapper;
import com.coma.comaroom.event.repository.EventApprovalRepository;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.XpManagementMapper;
import com.coma.comaroom.member.dto.response.XpManagementPageResponseDto;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private EventApprovalRepository eventApprovalRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private EventApprovalMapper eventApprovalMapper;
    @Mock private XpManagementMapper xpManagementMapper;

    @InjectMocks
    private AdminMemberService adminMemberService;

    private Member member;
    private EventApproval pendingApproval;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .studentId("20210001")
                .name("테스터")
                .password("encoded")
                .xp(100L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build();

        pendingApproval = EventApproval.builder()
                .id(1L)
                .approvalStatus(ApprovalStatus.PENDING)
                .grantedXp(50L)
                .reason("스터디 참여")
                .requester(member)
                .build();
    }

    // ─────────────────────────────────────────────
    // xpManagementPage
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("XP 관리 페이지 조회 성공 - 카운트 및 목록 반환")
    void xpManagementPage_success() {
        EventApproval approvedApproval = EventApproval.builder()
                .id(2L)
                .approvalStatus(ApprovalStatus.APPROVED)
                .grantedXp(30L)
                .reason("발표 참여")
                .requester(member)
                .build();

        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING)).thenReturn(1L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED)).thenReturn(1L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED)).thenReturn(0L);
        when(eventApprovalRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(approvedApproval, pendingApproval)));

        XpManagementPageResponseDto result = adminMemberService.xpManagementPage(0);

        assertThat(result.getPending()).isEqualTo(1L);
        assertThat(result.getApproved()).isEqualTo(1L);
        assertThat(result.getRejected()).isEqualTo(0L);
        assertThat(result.getEventApprovalResponseDtoList()).hasSize(2);
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("XP 관리 페이지 조회 - 승인 요청이 없을 때 빈 목록 반환")
    void xpManagementPage_empty() {
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING)).thenReturn(0L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED)).thenReturn(0L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED)).thenReturn(0L);
        when(eventApprovalRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 5), 0L));

        XpManagementPageResponseDto result = adminMemberService.xpManagementPage(0);

        assertThat(result.getPending()).isEqualTo(0L);
        assertThat(result.getApproved()).isEqualTo(0L);
        assertThat(result.getRejected()).isEqualTo(0L);
        assertThat(result.getEventApprovalResponseDtoList()).isEmpty();
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("XP 관리 페이지 조회 - DTO 필드 매핑 검증")
    void xpManagementPage_dtoMapping() {
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING)).thenReturn(1L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED)).thenReturn(0L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED)).thenReturn(0L);
        when(eventApprovalRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(pendingApproval)));

        XpManagementPageResponseDto result = adminMemberService.xpManagementPage(0);

        var dto = result.getEventApprovalResponseDtoList().get(0);
        assertThat(dto.getRequestId()).isEqualTo(1L);
        assertThat(dto.getRequester()).isEqualTo("테스터");
        assertThat(dto.getStudentId()).isEqualTo("20210001");
        assertThat(dto.getRewardXp()).isEqualTo(50L);
        assertThat(dto.getReason()).isEqualTo("스터디 참여");
        assertThat(dto.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
    }

    @Test
    @DisplayName("XP 관리 페이지 조회 - 2페이지 조회 시 currentPage 반환")
    void xpManagementPage_secondPage() {
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING)).thenReturn(6L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED)).thenReturn(0L);
        when(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED)).thenReturn(0L);
        when(eventApprovalRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(1, 5)))
                .thenReturn(new PageImpl<>(List.of(pendingApproval), PageRequest.of(1, 5), 6L));

        XpManagementPageResponseDto result = adminMemberService.xpManagementPage(1);

        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(6L);
        assertThat(result.getEventApprovalResponseDtoList()).hasSize(1);
    }

    // ─────────────────────────────────────────────
    // provisionXp
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("XP 지급 성공")
    void provisionXp_success() {
        XpProvisionRequestDto dto = mock(XpProvisionRequestDto.class);
        when(dto.getStudentId()).thenReturn("20210001");
        when(dto.getProvisionAmount()).thenReturn(50L);
        when(memberRepository.findByStudentId("20210001")).thenReturn(Optional.of(member));

        adminMemberService.provisionXp(dto);

        assertThat(member.getXp()).isEqualTo(150L);
    }

    @Test
    @DisplayName("XP 지급 실패 - 존재하지 않는 학번")
    void provisionXp_memberNotFound() {
        XpProvisionRequestDto dto = mock(XpProvisionRequestDto.class);
        when(dto.getStudentId()).thenReturn("99999999");
        when(memberRepository.findByStudentId("99999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminMemberService.provisionXp(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(AuthError.MEMBER_NOT_FOUND));
    }

    // ─────────────────────────────────────────────
    // decideProvision
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("XP 승인 결정 - 승인 시 XP 지급")
    void decideProvision_approved() {
        ProvisionApprovalRequestDto dto = mock(ProvisionApprovalRequestDto.class);
        when(dto.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);
        when(eventApprovalRepository.findById(1L)).thenReturn(Optional.of(pendingApproval));

        adminMemberService.decideProvision(dto, 1L);

        assertThat(pendingApproval.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(member.getXp()).isEqualTo(150L); // 100 + 50
    }

    @Test
    @DisplayName("XP 승인 결정 - 거절 시 XP 미지급")
    void decideProvision_rejected() {
        ProvisionApprovalRequestDto dto = mock(ProvisionApprovalRequestDto.class);
        when(dto.getApprovalStatus()).thenReturn(ApprovalStatus.REJECTED);
        when(eventApprovalRepository.findById(1L)).thenReturn(Optional.of(pendingApproval));

        adminMemberService.decideProvision(dto, 1L);

        assertThat(pendingApproval.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(member.getXp()).isEqualTo(100L); // XP 변화 없음
    }

    @Test
    @DisplayName("XP 승인 결정 실패 - 존재하지 않는 요청 ID")
    void decideProvision_approvalNotFound() {
        ProvisionApprovalRequestDto dto = mock(ProvisionApprovalRequestDto.class);
        when(eventApprovalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminMemberService.decideProvision(dto, 99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(EventError.APPROVAL_NOT_FOUND));
    }
}
