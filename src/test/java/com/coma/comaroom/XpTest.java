package com.coma.comaroom;

import com.coma.comaroom.auth.CustomUserDetails;
import com.coma.comaroom.event.dto.ProvisionApprovalRequestDto;
import com.coma.comaroom.event.dto.RequestProvisionXpDto;
import com.coma.comaroom.event.dto.XpManagementMainResponseDto;
import com.coma.comaroom.event.dto.XpProvisionRequestDto;
import com.coma.comaroom.event.entity.ApprovalStatus;
import com.coma.comaroom.event.entity.EventApproval;
import com.coma.comaroom.event.repository.EventApprovalRepository;
import com.coma.comaroom.event.service.XpService;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class XpTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SecurityUtils securityUtils;

    @Autowired
    XpService xpService;

    @Autowired
    EventApprovalRepository eventApprovalRepository;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
        Member member = Member.builder()
                .name("Jinwook")
                .xp(0L)
                .role(Role.ADMIN)
                .studentId("202121853")
                .password("20010410")
                .build();


        CustomUserDetails userDetails = new CustomUserDetails(member);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        memberRepository.save(member);
        assertNotNull(securityUtils.getCurrentMember());
    }

    @Test
    void provisionXpTest() {
        XpProvisionRequestDto xpProvisionRequestDto = XpProvisionRequestDto.builder()
                .studentId("202121853")
                .provisionAmount(3L)
                .provisionReason("test")
                .build();
        xpService.provisionXp(xpProvisionRequestDto);

        Member member = memberRepository.findByStudentId("202121853")
                .orElseThrow(() -> new IllegalStateException("멤버 없음"));
        assertEquals(3L, member.getXp());
    }

    @Test
    void decideProvisionTest() {
        RequestProvisionXpDto requestProvisionXpDto = RequestProvisionXpDto.builder()
                .provisionReason("test")
                .provisionAmount(10L)
                .build();
        xpService.requestProvisionXp(requestProvisionXpDto);

        ProvisionApprovalRequestDto provisionApprovalRequestDto = ProvisionApprovalRequestDto.builder()
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();


        Optional<EventApproval> eventApproval = eventApprovalRepository.findByApprovalStatus(ApprovalStatus.PENDING);
        xpService.decideProvision(provisionApprovalRequestDto, eventApproval.get().getId());



    }

    @Test
    void requestProvisionXpTest() {
        RequestProvisionXpDto requestProvisionXpDto = RequestProvisionXpDto.builder()
                .provisionReason("test")
                .provisionAmount(10L)
                .build();

        xpService.requestProvisionXp(requestProvisionXpDto);


    }

    @Test
    void getXpManagementMainDataTest() {

        for (int i = 1; i <= 10; i++) {
            EventApproval eventApproval = EventApproval.builder()
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .approvalAt(new Date()) // 승인된 상태니까 날짜 넣는 게 정상
                    .grantedXp(10L)
                    .reason("테스트 승인 데이터 " + i)
                    .requester(securityUtils.getCurrentMember())
                    .build();

            eventApprovalRepository.save(eventApproval);
        }

        XpManagementMainResponseDto xpManagementMainResponseDto = xpService.getXpManagementMainData(ApprovalStatus.APPROVED, 0L);

        System.out.println(xpManagementMainResponseDto);
    }




    @AfterEach
    void afterEach() {
        SecurityContextHolder.clearContext();
        memberRepository.deleteByStudentId("202121853");
    }
}
