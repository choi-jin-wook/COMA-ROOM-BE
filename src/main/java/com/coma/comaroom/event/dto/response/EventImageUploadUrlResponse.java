package com.coma.comaroom.event.dto.response;

public record EventImageUploadUrlResponse(
        String presignedUrl,
        String imageKey,
        String contentType,
        String url
) {
}
