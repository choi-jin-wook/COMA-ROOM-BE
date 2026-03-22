package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.request.EventPostRequest;
import com.coma.comaroom.event.dto.request.EventPostStatusRequest;
import com.coma.comaroom.event.dto.response.EventPostResponse;
import com.coma.comaroom.event.service.EventPostService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event-posts")
@AllArgsConstructor
public class EventPostController {
    private final EventPostService eventPostService;

    // CREATE (테스트 완료)
    @PostMapping
    public ResponseEntity<Response<EventPostResponse>> create(@RequestBody EventPostRequest request) {
        EventPostResponse eventPostResponse = eventPostService.createPost(request);
        return Response.ok(eventPostResponse, HttpStatus.CREATED).toResponseEntity();
    }

    // READ (단건 조회) (테스트 완료)
    @GetMapping("/{postId}")
    public ResponseEntity<Response<EventPostResponse>> getOne(@PathVariable Integer postId) {
        return Response.ok(eventPostService.getPost(postId), HttpStatus.OK).toResponseEntity();
    }

    // READ (전체 목록 조회) (테스트 완료)
    @GetMapping
    public ResponseEntity<Response<List<EventPostResponse>>> getAll() {
        return Response.ok(eventPostService.getAllPosts(), HttpStatus.OK).toResponseEntity();
    }

    // UPDATE (이건 수정 필요)
    @PatchMapping("/{postId}")
    public ResponseEntity<Response<EventPostResponse>> update(
            @PathVariable Integer postId,
            @RequestBody EventPostRequest request) {
        EventPostResponse data = eventPostService.updatePost(postId, request);
        return Response.ok(data, HttpStatus.OK).toResponseEntity();
    }

    // DELETE (테스트 완료)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Response<Void>> delete(
            @PathVariable Integer postId) {

        eventPostService.deletePost(postId);
        return Response.ok(HttpStatus.NO_CONTENT).toResponseEntity();
    }
}