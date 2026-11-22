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

/** 로그인 회원의 스터디 목록을 제공한다. */
@RestController
@RequestMapping("/api/member/studies")
@RequiredArgsConstructor
@Tag(name = "내 스터디", description = "로그인한 회원의 스터디 조회 API")
public class MemberStudyController {
    private final StudyService service;

    @GetMapping
    @Operation(summary = "내 스터디 목록 조회", description = "내 스터디 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<Response<Page<MyStudyResponse>>> mine(
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "20") String size) {
        return Response.ok(service.mine(page, size), HttpStatus.OK).toResponseEntity();
    }
}
