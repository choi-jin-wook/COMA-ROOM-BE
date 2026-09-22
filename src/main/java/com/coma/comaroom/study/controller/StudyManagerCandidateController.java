package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.service.StudyService;
import com.coma.comaroom.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/study/managers")
@RequiredArgsConstructor
public class StudyManagerCandidateController {
    private final StudyService service;

    @GetMapping
    public ResponseEntity<Response<StudyPage<StudyManagerCandidate>>> managers(
            @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Response.ok(service.managers(keyword, page, size), HttpStatus.OK).toResponseEntity();
    }
}
