package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.dto.request.AddStudyActivityRequest;
import com.coma.comaroom.study.dto.request.AddStudyMemberRequest;
import com.coma.comaroom.study.dto.request.CreateStudyRequest;
import com.coma.comaroom.study.dto.response.StudyActivityResponse;
import com.coma.comaroom.study.dto.response.StudyResponse;
import com.coma.comaroom.study.service.StudyManagerService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/study")
@AllArgsConstructor
public class StudyManagerController {

    private final StudyManagerService studyManagerService;

    // 스터디 생성
    @PostMapping
    public ResponseEntity<Response<StudyResponse>> createStudy(@RequestBody CreateStudyRequest request) {
        StudyResponse data = studyManagerService.createStudy(request);
        return Response.ok(data, HttpStatus.CREATED).toResponseEntity();
    }

    // 스터디 멤버 추가
    @PostMapping("/{studyId}/members")
    public ResponseEntity<Response<Void>> addStudyMember(
            @PathVariable Long studyId,
            @RequestBody AddStudyMemberRequest request) {
        studyManagerService.addStudyMember(studyId, request);
        return Response.ok(HttpStatus.CREATED).toResponseEntity();
    }

    // 스터디 멤버 삭제
    @DeleteMapping("/{studyId}/members/{memberId}")
    public ResponseEntity<Response<Void>> removeStudyMember(
            @PathVariable Long studyId,
            @PathVariable Long memberId) {
        studyManagerService.removeStudyMember(studyId, memberId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    // 스터디 일정 추가
    @PostMapping("/{studyId}/activities")
    public ResponseEntity<Response<StudyActivityResponse>> addStudyActivity(
            @PathVariable Long studyId,
            @RequestBody AddStudyActivityRequest request) {
        StudyActivityResponse data = studyManagerService.addStudyActivity(studyId, request);
        return Response.ok(data, HttpStatus.CREATED).toResponseEntity();
    }

    // 스터디 일정 삭제
    @DeleteMapping("/{studyId}/activities/{activityId}")
    public ResponseEntity<Response<Void>> removeStudyActivity(
            @PathVariable Long studyId,
            @PathVariable Long activityId) {
        studyManagerService.removeStudyActivity(studyId, activityId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }
}
