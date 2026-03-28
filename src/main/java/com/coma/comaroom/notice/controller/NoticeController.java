package com.coma.comaroom.notice.controller;

import com.coma.comaroom.notice.service.NoticeService;
import com.coma.comaroom.notice.dto.response.GetNoticeResponseDto;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/notice")
public class NoticeController {
    private final NoticeService noticeService;
    //  공지 페이징 조회 (테스트 완료)
    @GetMapping
    public ResponseEntity<?> getNotices(@RequestParam(defaultValue = "0") int page) {
        GetNoticeResponseDto getNoticeResponseDto  = noticeService.getNotices(page);
        return Response.ok(getNoticeResponseDto, HttpStatus.OK).toResponseEntity();
    }
}

