package com.coma.comaroom.event.service;

import com.coma.comaroom.event.dto.*;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.event.repository.EventApprovalRepository;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class XpService {
    private final MemberRepository memberRepository;
    private final EventApprovalRepository eventApprovalRepository;
    private final SecurityUtils securityUtils;

    public void provisionXp(XpProvisionRequestDto xpProvisionRequestDto) {
        Member member = memberRepository.findByStudentId(xpProvisionRequestDto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("해당 학생을 찾을 수 없습니다."));
        member.setXp(member.getXp() + xpProvisionRequestDto.getProvisionAmount());
    }


    public void decideProvision(ProvisionApprovalRequestDto provisionApprovalRequestDto, Long requestId) {
        EventApproval eventApproval = eventApprovalRepository.findById(requestId).orElseThrow(() -> new EntityNotFoundException("나중에 처리하지 뭐"));
        eventApproval.setApprovalStatus(provisionApprovalRequestDto.getApprovalStatus());
        Member currentUser = securityUtils.getCurrentMember();
        currentUser.getCreatedAt();
        currentUser.setXp(currentUser.getXp() + eventApproval.getGrantedXp());
    }

    public void requestProvisionXp(RequestProvisionXpDto requestProvisionXpDto) {
        Member currentUser = securityUtils.getCurrentMember();
        EventApproval eventApproval = EventApproval.builder()
                .requester(currentUser)
                .reason(requestProvisionXpDto.getProvisionReason())
                .approvalAt(null)
                .grantedXp(requestProvisionXpDto.getProvisionAmount())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        eventApprovalRepository.save(eventApproval);
    }

    public XpManagementMainResponseDto getXpManagementMainData(ApprovalStatus status, Long page) {
        // 1. 최근 등록순(DESC) + 상속받은 필드(createdAt) + 5개(size) 설정
        Pageable pageable = PageRequest.of(page.intValue(), 5, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. status null 체크해서 데이터 가져오기
        Page<EventApproval> resultPage = (status == null)
                ? eventApprovalRepository.findAllByOrderByCreatedAtDesc(pageable)
                : eventApprovalRepository.findByApprovalStatusOrderByCreatedAtDesc(status, pageable);

        // 3. 여기서 실제 객체 5개가 최근 순서대로 담김
        List<EventApproval> eventApprovals = resultPage.getContent();

        List<RecentActivityLogDto> recentActivityLogDtoList = new ArrayList<>();
        for (EventApproval approval : resultPage.getContent()) {
            recentActivityLogDtoList.add(RecentActivityLogDto.builder()
                    .id(approval.getId().longValue())
                    .userName(approval.getRequester().getName())
                    .studentId(approval.getRequester().getStudentId())
                    .description(approval.getReason())
                    .dateTime(approval.getCreatedAt())
                    .grantedXp(approval.getGrantedXp())
                    .status(approval.getApprovalStatus())
                    .build());
        }

        XpManagementMainResponseDto responseDto = XpManagementMainResponseDto.builder()
                .approvedCount(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED))
                .rejectedCount(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED))
                .pendingCount(eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING))
                .recentActivityLogs(recentActivityLogDtoList)
                .build();
        return responseDto;
    }
}
