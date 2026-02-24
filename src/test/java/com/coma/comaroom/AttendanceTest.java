package com.coma.comaroom;

import com.coma.comaroom.auth.CustomUserDetails;
import com.coma.comaroom.auth.SecurityConfig;
import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.CreateAttendanceRequestDto;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.service.AttendanceService;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class AttendanceTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
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
    void createAttendanceCheckTest(){
        CreateAttendanceCheckRequestDto createAttendanceCheckRequestDto = CreateAttendanceCheckRequestDto.builder()
                .eventTitle("정기회의")
                .eventCategory(EventCategory.COMPETITION)
                .expirationTime(10)
                .location("D409")
                .build();


        CreateAttendanceCheckResponseDto createAttendanceCheckResponseDto = attendanceService.createAttendanceCheck(createAttendanceCheckRequestDto);
        System.out.println(createAttendanceCheckResponseDto.getQrCodeId());
        assertNotNull(createAttendanceCheckResponseDto);
    }

    @Test
    void createAttendanceTest() {
        CreateAttendanceCheckRequestDto createAttendanceCheckRequestDto = CreateAttendanceCheckRequestDto.builder()
                .eventTitle("정기회의 8주차")
                .eventCategory(EventCategory.COMPETITION)
                .expirationTime(10)
                .location("D409")
                .build();


        CreateAttendanceCheckResponseDto createAttendanceCheckResponseDto = attendanceService.createAttendanceCheck(createAttendanceCheckRequestDto);

        CreateAttendanceRequestDto createAttendanceRequestDto = new CreateAttendanceRequestDto();
        createAttendanceRequestDto.setQrCodeId(createAttendanceCheckResponseDto.getQrCodeId());

        attendanceService.createAttendance(createAttendanceRequestDto);


    }

    @AfterEach
    void afterEach() {
        SecurityContextHolder.clearContext();
        memberRepository.deleteByStudentId("202121853");
    }
}
