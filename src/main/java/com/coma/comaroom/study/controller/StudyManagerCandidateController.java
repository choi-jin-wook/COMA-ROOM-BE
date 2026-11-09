package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.service.StudyService;
import com.coma.comaroom.utils.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 관리자에게 스터디장으로 지정할 회원 후보를 제공한다. */
@RestController
@RequestMapping("/api/admin/study/managers")
@RequiredArgsConstructor
@Tag(name = "스터디 관리", description = "관리자의 스터디 생성 및 회원 관리 API")
public class StudyManagerCandidateController {
    private final StudyService service;

    @GetMapping
    @Operation(summary = "스터디장 후보 조회", description = "이름 또는 학번 키워드로 스터디장 후보 회원을 페이지 단위로 조회합니다.")
    public ResponseEntity<Response<Page<StudyManagerCandidate>>> managers(
            @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Response.ok(service.managers(keyword, page, size), HttpStatus.OK).toResponseEntity();
    }
}
