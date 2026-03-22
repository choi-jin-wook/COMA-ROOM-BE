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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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




}
