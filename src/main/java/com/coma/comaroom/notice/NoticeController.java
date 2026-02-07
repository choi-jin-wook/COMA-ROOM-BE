package com.coma.comaroom.notice;


import com.coma.comaroom.notice.dto.request.CreateNoticeRequestDto;
import com.coma.comaroom.notice.dto.request.UpdateNoticeRequestDto;
import com.coma.comaroom.notice.dto.response.CreateNoticeResponseDto;
import com.coma.comaroom.notice.dto.response.UpdateNoticeResponseDto;
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

    // 1. 공지 등록
    @PostMapping
    public ResponseEntity<Response<CreateNoticeResponseDto>> createNotice(@RequestBody CreateNoticeRequestDto requestDto) {
        CreateNoticeResponseDto createNoticeResponseDto = noticeService.createNotice(requestDto);
        return Response.ok(createNoticeResponseDto, HttpStatus.CREATED).toResponseEntity();
    }

    // 2. 공지 수정
    @PatchMapping("/{noticeId}")
    public ResponseEntity<Response<UpdateNoticeResponseDto>> updateNotice(@PathVariable Long noticeId, @RequestBody UpdateNoticeRequestDto updateNoticeRequestDto) {
        UpdateNoticeResponseDto updateNoticeResponseDto = noticeService.updateNotice(noticeId, updateNoticeRequestDto);
        return Response.ok(updateNoticeResponseDto, HttpStatus.OK).toResponseEntity();
    }

    // 3. 공지 삭제
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Response<Void>> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }

    // 4. 공지 페이징 조회
    @GetMapping
    public ResponseEntity<?> getNotices(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        // TODO: service.getNotices(page, size);
        return ResponseEntity.ok().build();
    }

    // 5. 고정 처리
}

