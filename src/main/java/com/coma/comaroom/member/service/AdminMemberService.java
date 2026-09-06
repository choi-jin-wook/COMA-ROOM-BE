package com.coma.comaroom.member.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.dto.*;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.repository.EventApprovalRepository;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.member.dto.response.EventApprovalResponseDto;
import com.coma.comaroom.member.dto.response.MemberInformationResponseDto;
import com.coma.comaroom.member.dto.response.MemberManagementPageRequestDto;
import com.coma.comaroom.member.dto.response.XpManagementPageResponseDto;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class AdminMemberService {
    private final MemberRepository memberRepository;
    private final EventApprovalRepository eventApprovalRepository;
    private final EventParticipateRepository eventParticipateRepository;
    private final SecurityUtils securityUtils;

    private static final int PAGE_SIZE = 5;

    @Transactional(readOnly = true)
    public MemberManagementPageRequestDto memberManagementPage(int page) {
        long totalMember = memberRepository.count();
        long averageXp = memberRepository.findAverageXp().longValue();

        Page<Member> memberPage = memberRepository
                .findPageByOrderByXpDescMemberIdAsc(PageRequest.of(page, PAGE_SIZE));

        List<MemberInformationResponseDto> dtoList = memberPage.getContent().stream()
                .map(member -> MemberInformationResponseDto.builder()
                        .name(member.getName())
                        .studentId(member.getStudentId())
                        .major(member.getMajor().getMajor())
                        .xp(member.getXp())
                        .eventAttendance(eventParticipateRepository
                                .countByParticipantMemberAndEvent_EventCategory(member, EventCategory.EVENT))
                        .meetingAttendance(eventParticipateRepository
                                .countByParticipantMemberAndEvent_EventCategory(member, EventCategory.REGULAR_MEETING))
                        .build())
                .toList();

        return MemberManagementPageRequestDto.builder()
                .totalMember(totalMember)
                .activateMember(totalMember)
                .averageXp(averageXp)
                .memberInformationResponseDtos(dtoList)
                .currentPage(memberPage.getNumber())
                .totalPages(memberPage.getTotalPages())
                .totalElements(memberPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public XpManagementPageResponseDto xpManagementPage(int page) {
        Long pending = eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        Long approved = eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        Long rejected = eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED);

        Page<EventApproval> approvalPage = eventApprovalRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, PAGE_SIZE));

        List<EventApprovalResponseDto> dtoList = approvalPage.getContent().stream()
                .map(approval -> EventApprovalResponseDto.builder()
                        .requestId(approval.getId())
                        .requester(approval.getRequester() != null ? approval.getRequester().getName() : "탈퇴한 회원")
                        .studentId(approval.getRequester() != null ? approval.getRequester().getStudentId() : "-")
                        .rewardXp(approval.getGrantedXp())
                        .reason(approval.getReason())
                        .localDateTime(approval.getCreatedAt())
                        .approvalStatus(approval.getApprovalStatus())
                        .build())
                .toList();

        return XpManagementPageResponseDto.builder()
                .pending(pending)
                .approved(approved)
                .rejected(rejected)
                .eventApprovalResponseDtoList(dtoList)
                .currentPage(approvalPage.getNumber())
                .totalPages(approvalPage.getTotalPages())
                .totalElements(approvalPage.getTotalElements())
                .build();
    }

    public void provisionXp(XpProvisionRequestDto xpProvisionRequestDto) {
        Member member = memberRepository.findByStudentId(xpProvisionRequestDto.getStudentId())
                .orElseThrow(() -> new BusinessException(AuthError.MEMBER_NOT_FOUND));
        member.setXp(member.getXp() + xpProvisionRequestDto.getProvisionAmount());
    }

    public void decideProvision(ProvisionApprovalRequestDto provisionApprovalRequestDto, Long requestId) {
        EventApproval eventApproval = eventApprovalRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(EventError.APPROVAL_NOT_FOUND));

        // 이미 처리된 요청을 다시 승인하면 XP가 중복 지급되므로 PENDING 상태만 처리 가능
        if (eventApproval.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(EventError.APPROVAL_ALREADY_DECIDED);
        }

        Member requester = eventApproval.getRequester();
        if (requester == null) {
            // 탈퇴한 회원의 요청은 처리 불가
            throw new BusinessException(AuthError.MEMBER_NOT_FOUND);
        }

        eventApproval.setApprovalStatus(provisionApprovalRequestDto.getApprovalStatus());
        if (provisionApprovalRequestDto.getApprovalStatus() == ApprovalStatus.APPROVED) {
            requester.setXp(requester.getXp() + eventApproval.getGrantedXp());
        }
    }
}
