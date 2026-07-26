package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.request.EventPostRequest;
import com.coma.comaroom.event.dto.response.EventPostResponse;
import com.coma.comaroom.event.service.EventPostService;
import com.coma.comaroom.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event-posts")
@AllArgsConstructor
public class EventPostController {
    private final EventPostService eventPostService;

    // CREATE (테스트 완료)
    @PostMapping
    public ResponseEntity<Response<EventPostResponse>> createPost(@RequestBody EventPostRequest request) {
        return Response.ok(eventPostService.createPost(request), HttpStatus.CREATED).toResponseEntity();
    }

    // READ (단건 조회) (테스트 완료)
    @GetMapping("/{postId}")
    public ResponseEntity<Response<EventPostResponse>> getOnePost(@PathVariable Integer postId) {
        return Response.ok(eventPostService.getPost(postId), HttpStatus.OK).toResponseEntity();
    }

    // READ (전체 목록 조회)
    @GetMapping
    public ResponseEntity<Response<Page<EventPostResponse>>> getAllPost(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return Response.ok(eventPostService.getAllPosts(pageable), HttpStatus.OK).toResponseEntity();
    }

}