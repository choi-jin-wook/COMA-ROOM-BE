package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.request.CreateEventRequest;
import com.coma.comaroom.event.dto.request.EventRequest;
import com.coma.comaroom.event.dto.response.EventResponse;
import com.coma.comaroom.event.service.AdminEventService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin/event")
public class AdminEventController {
    private final AdminEventService adminEventService;
    // 이벤트 생성 (테스트 완료)
    @PostMapping()
    public ResponseEntity<Response<EventResponse>> createEvent(@RequestBody CreateEventRequest request) {
        EventResponse data = adminEventService.createEvent(request);
        return Response.ok(data, HttpStatus.CREATED).toResponseEntity();
    }

    // 출석 생성
    @PostMapping("/attendances")// (포스트맨 테스트 완료)
    public ResponseEntity<Response<CreateAttendanceCheckResponseDto>> createAttendanceCheck(@RequestBody CreateAttendanceCheckRequestDto createAttendanceCheckRequestDto) {
        CreateAttendanceCheckResponseDto createAttendanceCheckResponseDto = adminEventService.createAttendanceCheck(createAttendanceCheckRequestDto);
        return Response.ok(createAttendanceCheckResponseDto, HttpStatus.CREATED).toResponseEntity();
    }

    // 삭제 (테스트 완료)
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Response<Void>> deleteEvent(@PathVariable Long eventId) {
        adminEventService.deleteEvent(eventId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    // 수정 (테스트 완료)
    @PatchMapping("/{eventId}")
    public ResponseEntity<Response<EventResponse>> updateEvent(
            @PathVariable Long eventId,
            @RequestBody EventRequest request) {
        return Response.ok(adminEventService.updateEvent(eventId, request), HttpStatus.OK).toResponseEntity();
    }
}
