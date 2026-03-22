package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.request.EventPostStatusRequest;
import com.coma.comaroom.event.dto.response.EventPostResponse;
import com.coma.comaroom.event.service.AdminEventPostService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/event-posts")
@AllArgsConstructor
public class AdminEventPostController {
    private final AdminEventPostService adminEventPostService;

    @PatchMapping("/{postId}/status")// (테스트 완료)
    public ResponseEntity<Response<EventPostResponse>> updateStatus(
            @PathVariable Integer postId,
            @RequestBody EventPostStatusRequest request) {

        EventPostResponse data = adminEventPostService.updatePostStatus(postId, request);
        return Response.ok(data, HttpStatus.OK).toResponseEntity();
    }
}
