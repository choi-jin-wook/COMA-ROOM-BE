package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.request.EventPostRequest;
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


    // UPDATE (이건 수정 필요)
    @PatchMapping("/{postId}")
    public ResponseEntity<Response<EventPostResponse>> update(
            @PathVariable Integer postId,
            @RequestBody EventPostRequest request) {
        EventPostResponse data = adminEventPostService.updatePost(postId, request);
        return Response.ok(data, HttpStatus.OK).toResponseEntity();
    }

    // DELETE (테스트 완료)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Response<Void>> delete(
            @PathVariable Integer postId) {

        adminEventPostService.deletePost(postId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }
}
