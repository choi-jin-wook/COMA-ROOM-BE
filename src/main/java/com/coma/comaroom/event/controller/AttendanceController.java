package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.CreateAttendanceRequestDto;
import com.coma.comaroom.event.service.AttendanceService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class AttendanceController {
    private final AttendanceService attendanceService;

    // 출석 생성
    @PostMapping("/admin/attendances")// (포스트맨 테스트 완료)
    public ResponseEntity<Response<CreateAttendanceCheckResponseDto>> createAttendanceCheck(@RequestBody CreateAttendanceCheckRequestDto createAttendanceCheckRequestDto) {
        CreateAttendanceCheckResponseDto createAttendanceCheckResponseDto = attendanceService.createAttendanceCheck(createAttendanceCheckRequestDto);
        return Response.ok(createAttendanceCheckResponseDto, HttpStatus.CREATED).toResponseEntity();
    }

    // 출석처리
    @PostMapping("/attendances/checks") // (포스트맨 테스트 완료)
    public ResponseEntity<?> recordAttendanceCheck(@RequestBody CreateAttendanceRequestDto createAttendanceRequestDto) {
        attendanceService.createAttendance(createAttendanceRequestDto);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }
}