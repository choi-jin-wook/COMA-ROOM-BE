package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.entity.StudyStatus;
import com.coma.comaroom.study.service.StudyService;
import com.coma.comaroom.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member/studies")
@RequiredArgsConstructor
public class MemberStudyController {
    private final StudyService service;

    @GetMapping("/summary")
    public ResponseEntity<Response<StudySummary>> summary() {
        return Response.ok(service.summary(), HttpStatus.OK).toResponseEntity();
    }

    @GetMapping
    public ResponseEntity<Response<StudyPage<MyStudyResponse>>> mine(
            @RequestParam StudyStatus status, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Response.ok(service.mine(status, page, size), HttpStatus.OK).toResponseEntity();
    }
}
