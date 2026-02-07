package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.CreateAttendanceRequestDto;
import com.coma.comaroom.event.service.AttendanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/attendances")
public class AttendanceController {
    private final AttendanceService attendanceService;

    // 출석 생성
    @PostMapping()// (포스트맨 테스트 완료)
    public ResponseEntity<?> createAttendanceCheck(@RequestBody CreateAttendanceCheckRequestDto createAttendanceCheckRequestDto) {
        CreateAttendanceCheckResponseDto createAttendanceCheckResponseDto = attendanceService.createAttendanceCheck(createAttendanceCheckRequestDto);
        return ResponseEntity.ok(createAttendanceCheckResponseDto);
    }

    // 출석처리
    @PostMapping("/checks") // (포스트맨 테스트 완료)
    public ResponseEntity<?> recordAttendanceCheck(@RequestBody CreateAttendanceRequestDto createAttendanceRequestDto) {
        attendanceService.createAttendance(createAttendanceRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
