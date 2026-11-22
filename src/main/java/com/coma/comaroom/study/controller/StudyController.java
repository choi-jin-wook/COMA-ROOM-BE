package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.dto.request.*;
import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.service.*;
import com.coma.comaroom.utils.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.nio.charset.StandardCharsets;

/** 회원의 스터디 탐색, 주차 학습 및 출석 요청을 서비스에 연결한다. */
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
@Tag(name = "스터디", description = "스터디 모집, 참여 신청, 주차별 학습 및 출석 API")
public class StudyController {
    private final StudyJoinService joins;
    private final StudyWeekService weeks;
    private final StudyAttendanceService attendances;

    @GetMapping("/recruiting")
    @Operation(summary = "모집 중인 스터디 조회", description = "참여 가능한 스터디 목록을 페이지 단위로 조회합니다.")
    public ResponseEntity<Response<Page<RecruitingStudyResponse>>> recruiting(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return Response.ok(joins.recruiting(page, size), HttpStatus.OK).toResponseEntity();
    }

    @PostMapping("/{studyId}/join-requests")
    @Operation(summary = "스터디 참여 신청", description = "로그인한 회원이 지정한 스터디에 참여를 신청합니다.")
    public ResponseEntity<Response<StudyJoinRequestResponse>> join(@PathVariable Long studyId) {
        return Response.ok(joins.request(studyId), HttpStatus.CREATED).toResponseEntity();
    }

    @GetMapping("/{studyId}/weeks")
    @Operation(summary = "스터디 주차 목록 조회", description = "스터디 정보와 1~16주차 계획 목록을 조회합니다.")
    public ResponseEntity<Response<StudyWeeksResponse>> weeks(@PathVariable Long studyId) {
        return Response.ok(weeks.weeks(studyId), HttpStatus.OK).toResponseEntity();
    }

    @GetMapping("/{studyId}/weeks/{weekNumber}")
    @Operation(summary = "스터디 주차 상세 조회", description = "지정한 주차의 계획과 첨부 자료를 조회합니다.")
    public ResponseEntity<Response<StudyWeekResponse>> week(@PathVariable Long studyId, @PathVariable int weekNumber) {
        return Response.ok(weeks.detail(studyId, weekNumber), HttpStatus.OK).toResponseEntity();
    }

    @PostMapping(value = "/{studyId}/weeks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "스터디 주차 계획 생성", description = "스터디장이 주차 계획을 생성합니다. multipart/form-data의 request JSON 파트는 필수이며 file 파트는 선택 사항입니다.")
    public ResponseEntity<Response<StudyWeekResponse>> createWeek(@PathVariable Long studyId,
            @Valid @RequestPart("request") CreateStudyWeekRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return Response.ok(weeks.create(studyId, request, file), HttpStatus.CREATED).toResponseEntity();
    }

    @GetMapping("/{studyId}/weeks/{weekNumber}/materials/{materialId}/download")
    @Operation(summary = "스터디 자료 다운로드 URL 조회", description = "지정한 주차의 자료에 대한 유효기간이 있는 다운로드 URL을 발급합니다.")
    public ResponseEntity<Response<StudyMaterialDownloadResponse>> downloadUrl(@PathVariable Long studyId,
            @PathVariable int weekNumber, @PathVariable Long materialId) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return Response.ok(weeks.downloadUrl(studyId, weekNumber, materialId, baseUrl), HttpStatus.OK).toResponseEntity();
    }

    @GetMapping("/{studyId}/weeks/{weekNumber}/materials/{materialId}/content")
    @Operation(summary = "스터디 자료 다운로드", description = "발급된 URL의 expires와 signature를 검증한 뒤 자료 파일을 내려받습니다.")
    public ResponseEntity<byte[]> download(@PathVariable Long studyId, @PathVariable int weekNumber,
            @PathVariable Long materialId, @RequestParam long expires, @RequestParam String signature) {
        StudyMaterialContent material = weeks.download(studyId, weekNumber, materialId, expires, signature);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(material.sizeBytes()).cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(material.fileName(), StandardCharsets.UTF_8).build().toString())
                .body(material.content());
    }

    @PostMapping("/{studyId}/weeks/{weekNumber}/attendances")
    @Operation(summary = "스터디 주차 출석 세션 생성", description = "스터디장이 기존 스터디 이벤트의 QR 출석을 생성합니다. eventId는 스터디장이 주최한 스터디 이벤트 ID이고 expirationTime은 유효 시간(분)입니다.")
    public ResponseEntity<Response<StudyAttendanceResponse>> createAttendance(@PathVariable Long studyId,
            @PathVariable int weekNumber, @Valid @RequestBody CreateStudyAttendanceRequest request) {
        return Response.ok(attendances.create(studyId, weekNumber, request), HttpStatus.CREATED).toResponseEntity();
    }
}
