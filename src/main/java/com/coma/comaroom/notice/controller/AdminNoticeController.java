package com.coma.comaroom.notice.controller;

import com.coma.comaroom.notice.dto.request.CreateNoticeRequestDto;
import com.coma.comaroom.notice.dto.request.UpdateNoticeRequestDto;
import com.coma.comaroom.notice.dto.response.CreateNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.GetNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.UpdateNoticeResponseDto;
import com.coma.comaroom.notice.service.AdminNoticeService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/admin/notice")
public class AdminNoticeController {
    private final AdminNoticeService adminNoticeService;
    // 1. 공지 등록 (테스트 완료)
    @PostMapping
    public ResponseEntity<Response<CreateNoticeResponseDto>> createNotice(@RequestBody CreateNoticeRequestDto requestDto) {
        CreateNoticeResponseDto createNoticeResponseDto = adminNoticeService.createNotice(requestDto);
        return Response.ok(createNoticeResponseDto, HttpStatus.CREATED).toResponseEntity();
    }

    // 2. 공지 수정 (테스트 완료)
    @PatchMapping("/{noticeId}")
    public ResponseEntity<Response<UpdateNoticeResponseDto>> updateNotice(@PathVariable Long noticeId, @RequestBody UpdateNoticeRequestDto updateNoticeRequestDto) {
        UpdateNoticeResponseDto updateNoticeResponseDto = adminNoticeService.updateNotice(noticeId, updateNoticeRequestDto);
        return Response.ok(updateNoticeResponseDto, HttpStatus.OK).toResponseEntity();
    }

    // 3. 공지 삭제 (테스트 완료)
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Response<Void>> deleteNotice(@PathVariable Long noticeId) {
        adminNoticeService.deleteNotice(noticeId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }


    // 4. 고정 처리 (테스트 완료)
    @PatchMapping("/{noticeId}/pinned")
    public ResponseEntity<Response<Void>> pinnedNotice(@PathVariable Long noticeId) {
        adminNoticeService.pinnedNotice(noticeId);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }


    // 5. 숨김 처리 (테스트 완료)
    @PatchMapping("/{noticeId}/hidden")
    public ResponseEntity<Response<Void>> hiddenNotice(@PathVariable Long noticeId) {
        adminNoticeService.hiddenNotice(noticeId);
        return Response.ok(HttpStatus.OK).toResponseEntity();
    }

}
