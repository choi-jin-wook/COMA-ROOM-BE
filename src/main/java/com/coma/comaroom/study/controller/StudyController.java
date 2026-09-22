package com.coma.comaroom.study.controller;

import com.coma.comaroom.study.dto.request.*;
import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.service.*;
import com.coma.comaroom.utils.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {
    private final StudyJoinService joins;
    private final StudyWeekService weeks;
    private final StudyAttendanceService attendances;

    @GetMapping("/recruiting")
    public ResponseEntity<Response<StudyPage<RecruitingStudyResponse>>> recruiting(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return Response.ok(joins.recruiting(page, size), HttpStatus.OK).toResponseEntity();
    }

    @PostMapping("/{studyId}/join-requests")
    public ResponseEntity<Response<StudyJoinRequestResponse>> join(@PathVariable Long studyId) {
        return Response.ok(joins.request(studyId), HttpStatus.CREATED).toResponseEntity();
    }

    @GetMapping("/{studyId}/weeks")
    public ResponseEntity<Response<StudyWeeksResponse>> weeks(@PathVariable Long studyId) {
        return Response.ok(weeks.weeks(studyId), HttpStatus.OK).toResponseEntity();
    }

    @GetMapping("/{studyId}/weeks/{weekNumber}")
    public ResponseEntity<Response<StudyWeekResponse>> week(@PathVariable Long studyId, @PathVariable int weekNumber) {
        return Response.ok(weeks.detail(studyId, weekNumber), HttpStatus.OK).toResponseEntity();
    }

    @PostMapping(value = "/{studyId}/weeks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<StudyWeekResponse>> createWeek(@PathVariable Long studyId,
            @Valid @RequestPart("request") CreateStudyWeekRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return Response.ok(weeks.create(studyId, request, file), HttpStatus.CREATED).toResponseEntity();
    }

    @GetMapping("/{studyId}/weeks/{weekNumber}/materials/{materialId}/download")
    public ResponseEntity<Response<StudyMaterialDownloadResponse>> downloadUrl(@PathVariable Long studyId,
            @PathVariable int weekNumber, @PathVariable Long materialId) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return Response.ok(weeks.downloadUrl(studyId, weekNumber, materialId, baseUrl), HttpStatus.OK).toResponseEntity();
    }

    @GetMapping("/{studyId}/weeks/{weekNumber}/materials/{materialId}/content")
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
    public ResponseEntity<Response<StudyAttendanceResponse>> createAttendance(@PathVariable Long studyId,
            @PathVariable int weekNumber, @Valid @RequestBody CreateStudyAttendanceRequest request) {
        return Response.ok(attendances.create(studyId, weekNumber, request), HttpStatus.CREATED).toResponseEntity();
    }
}
