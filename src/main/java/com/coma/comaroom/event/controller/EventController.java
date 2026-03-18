package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.CreateAttendanceCheckRequestDto;
import com.coma.comaroom.event.dto.CreateAttendanceCheckResponseDto;
import com.coma.comaroom.event.dto.CreateAttendanceRequestDto;
import com.coma.comaroom.event.dto.request.EventRequest;
import com.coma.comaroom.event.dto.response.EventResponse;
import com.coma.comaroom.event.service.EventService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/monthly")
    public ResponseEntity<Response<List<EventResponse>>> getMonthlyEvents(
            @RequestParam int year,
            @RequestParam int month) {
        return Response.ok(eventService.getMonthlyEvents(year, month), HttpStatus.OK).toResponseEntity();
    }

    @PostMapping
    public ResponseEntity<Response<EventResponse>> create(@RequestBody EventRequest request) {
        // 컨트롤러에서 Member를 받지 않음
        return Response.ok(eventService.createEvent(request), HttpStatus.CREATED).toResponseEntity();
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Response<Void>> delete(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<Response<EventResponse>> update(
            @PathVariable Long eventId,
            @RequestBody EventRequest request) {
        return Response.ok(eventService.updateEvent(eventId, request), HttpStatus.OK).toResponseEntity();
    }




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
