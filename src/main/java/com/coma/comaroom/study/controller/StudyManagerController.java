package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.dto.request.AddStudyMemberRequest;
import com.coma.comaroom.study.dto.request.CreateStudyRequest;
import com.coma.comaroom.study.dto.response.StudyResponse;
import com.coma.comaroom.study.service.StudyManagerService;
import com.coma.comaroom.utils.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 관리자용 스터디 생성과 멤버 관리 API를 제공한다. */
@RestController
@RequestMapping("/api/admin/study")
@AllArgsConstructor
@Tag(name = "스터디 관리", description = "관리자의 스터디 생성 및 회원 관리 API")
public class StudyManagerController {

    private final StudyManagerService studyManagerService;

    // 스터디 생성
    @PostMapping
    @Operation(summary = "스터디 생성", description = "관리자가 스터디 이름과 스터디장 회원 ID로 스터디를 생성합니다.")
    public ResponseEntity<Response<StudyResponse>> createStudy(@Valid @RequestBody CreateStudyRequest request) {
        StudyResponse data = studyManagerService.createStudy(request);
        return Response.ok(data, HttpStatus.CREATED).toResponseEntity();
    }

    // 스터디 멤버 추가
    @PostMapping("/{studyId}/members")
    @Operation(summary = "스터디 회원 추가", description = "관리자가 지정한 회원을 스터디에 추가합니다.")
    public ResponseEntity<Response<Void>> addStudyMember(
            @PathVariable Long studyId,
            @RequestBody AddStudyMemberRequest request) {
        studyManagerService.addStudyMember(studyId, request);
        return Response.ok(HttpStatus.CREATED).toResponseEntity();
    }

    // 스터디 멤버 삭제
    @DeleteMapping("/{studyId}/members/{memberId}")
    @Operation(summary = "스터디 회원 삭제", description = "관리자가 지정한 회원을 스터디에서 제거합니다.")
    public ResponseEntity<Response<Void>> removeStudyMember(
            @PathVariable Long studyId,
            @PathVariable Long memberId) {
        studyManagerService.removeStudyMember(studyId, memberId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }

}
