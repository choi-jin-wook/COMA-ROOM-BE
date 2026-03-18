package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.CreateAttendanceRequestDto;
import com.coma.comaroom.event.service.EventService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EventController {
    // 1. 이달의 이벤트 조회

    // 2. 이벤트 생성

    // 3. 이벤트 삭제

    // 4. 이벤트 수정



    private final EventService eventService;

    // 출석 생성
    @PostMapping("/admin/attendances")// (포스트맨 테스트 완료)
    public ResponseEntity<Response<CreateAttendanceCheckResponseDto>> createAttendanceCheck(@RequestBody CreateAttendanceCheckRequestDto createAttendanceCheckRequestDto) {
        CreateAttendanceCheckResponseDto createAttendanceCheckResponseDto = eventService.createAttendanceCheck(createAttendanceCheckRequestDto);
        return Response.ok(createAttendanceCheckResponseDto, HttpStatus.CREATED).toResponseEntity();
    }

    // 출석처리
    @PostMapping("/attendances/checks") // (포스트맨 테스트 완료)
    public ResponseEntity<?> recordAttendanceCheck(@RequestBody CreateAttendanceRequestDto createAttendanceRequestDto) {
        eventService.createAttendance(createAttendanceRequestDto);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }

    // 출석 조정

    // 출석 명단


}
