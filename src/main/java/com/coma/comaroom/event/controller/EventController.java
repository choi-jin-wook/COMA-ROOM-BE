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
@RequestMapping("api/event")
public class EventController {
    private final EventService eventService;

    // 이달의 행사 조회  (테스트 완료)
    @GetMapping("/monthly")
    public ResponseEntity<Response<List<EventResponse>>> getMonthlyEvents(
            @RequestParam int year,
            @RequestParam int month) {
        return Response.ok(eventService.getMonthlyEvents(year, month), HttpStatus.OK).toResponseEntity();
    }



    // 출석처리 (테스트 완료)
    @PostMapping("/attendances/checks") // (포스트맨 테스트 완료)
    public ResponseEntity<?> recordAttendanceCheck(@RequestBody CreateAttendanceRequestDto createAttendanceRequestDto) {
        eventService.createAttendance(createAttendanceRequestDto);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }




}
