package com.coma.comaroom.member.service;

import com.coma.comaroom.event.dto.*;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.event.mapper.EventApprovalMapper;
import com.coma.comaroom.member.XpManagementMapper;
import com.coma.comaroom.event.repository.EventApprovalRepository;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class AdminMemberService {
    private final MemberRepository memberRepository;
    private final EventApprovalRepository eventApprovalRepository;
    private final SecurityUtils securityUtils;
    private final EventApprovalMapper eventApprovalMapper;
    private final XpManagementMapper xpManagementMapper;

    public void provisionXp(XpProvisionRequestDto xpProvisionRequestDto) {
        Member member = memberRepository.findByStudentId(xpProvisionRequestDto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("해당 학생을 찾을 수 없습니다."));
        member.setXp(member.getXp() + xpProvisionRequestDto.getProvisionAmount());
    }


    public void decideProvision(ProvisionApprovalRequestDto provisionApprovalRequestDto, Long requestId) {
        EventApproval eventApproval = eventApprovalRepository.findById(requestId).orElseThrow(() -> new EntityNotFoundException("나중에 처리하지 뭐"));
        eventApproval.setApprovalStatus(provisionApprovalRequestDto.getApprovalStatus());
        Member requester = eventApproval.getRequester();
        if (provisionApprovalRequestDto.getApprovalStatus() == ApprovalStatus.APPROVED) {
            requester.setXp(requester.getXp() + eventApproval.getGrantedXp());
        }
    }

    public XpPetitionResponseDto requestProvisionXp(XpPetitionRequestDto xpPetitionRequestDto) {
        Member currentUser = securityUtils.getCurrentMember();
        EventApproval eventApproval = EventApproval.requestXpApproval(currentUser, xpPetitionRequestDto.getProvisionReason(), xpPetitionRequestDto.getProvisionAmount());
        eventApprovalRepository.save(eventApproval);
        XpPetitionResponseDto xpPetitionResponseDto = new XpPetitionResponseDto();

        return xpPetitionResponseDto;
    }

    public XpManagementMainResponseDto getXpManagementMainData(ApprovalStatus status, Long page) {
        // 1. 최근 등록순(DESC) + 상속받은 필드(createdAt) + 5개(size) 설정
        Pageable pageable = PageRequest.of(page.intValue(), 5, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. status null 체크해서 데이터 가져오기
        Page<EventApproval> resultPage = (status == null)
                ? eventApprovalRepository.findAllByOrderByCreatedAtDesc(pageable)
                : eventApprovalRepository.findByApprovalStatusOrderByCreatedAtDesc(status, pageable);

        // 3. 여기서 실제 객체 5개가 최근 순서대로 담김
        // 여기서 꺼내고
        List<EventApproval> eventApprovals = resultPage.getContent();

        // 여기다 쓴다
        List<RecentActivityLogDto> recentActivityLogs =
                eventApprovalMapper.toRecentActivityLogDtos(eventApprovals);

        return xpManagementMapper.toMainDto(
                eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED),
                eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED),
                eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING),
                recentActivityLogs
        );
    }
}
