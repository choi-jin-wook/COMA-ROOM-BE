package com.coma.comaroom.event.controller;

import com.coma.comaroom.event.dto.request.EventImageUploadUrlRequest;
import com.coma.comaroom.event.dto.response.EventImageUploadUrlResponse;
import com.coma.comaroom.event.service.EventImageUploadService;
import com.coma.comaroom.utils.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-posts/files")
@RequiredArgsConstructor
public class EventImageUploadController {

    private final EventImageUploadService eventImageUploadService;

    @PostMapping("/presigned-url")
    public ResponseEntity<Response<EventImageUploadUrlResponse>> createUploadUrl(
            @Valid @RequestBody EventImageUploadUrlRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Response.ok(eventImageUploadService.createUploadUrl(request), HttpStatus.OK));
    }
}
