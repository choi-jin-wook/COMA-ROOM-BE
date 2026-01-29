package com.coma.comaroom.event.service;

import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.CreateAttendanceRequestDto;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Base64;


import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AttendanceService {
    private final SecurityUtils securityUtils;
    private final AttendanceRepository attendanceRepository;

    public CreateAttendanceCheckResponseDto createAttendanceCheck(CreateAttendanceCheckRequestDto createAttendanceCheckRequestDto) {
        Member currentUser = securityUtils.getCurrentMember();

        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder()
                .build();

        attendanceRepository.save(activity);

        String qrCodeId = Base64.getEncoder().encodeToString(activity.getName().getBytes()) + "-" + Base64.getEncoder().encodeToString(activity.getActivityType().toString().getBytes());

        CreateAttendanceCheckResponseDto createAttendanceCheckResponseDto = CreateAttendanceCheckResponseDto.builder()
                .qrCodeId(qrCodeId)
                .build();
        // base64로 인코딩한 값으로 (행사이름-열거형)

        return createAttendanceCheckResponseDto;
    }

    public void createAttendance(CreateAttendanceRequestDto createAttendanceRequestDto) {
        String[] qrCodeId = createAttendanceRequestDto.getQrCodeId().split("-");
        ActivityType activityType = ActivityType.valueOf(qrCodeId[1]);

        Optional<Activity> activity = attendanceRepository.findByNameAndActivityType(qrCodeId[0], activityType);
        Member currentUser = securityUtils.getCurrentMember();

        ActivityParticipant activityParticipant = ActivityParticipant.builder()
                .activity(activity.get())
                .participantMember(currentUser)
                .build();
    }
}
