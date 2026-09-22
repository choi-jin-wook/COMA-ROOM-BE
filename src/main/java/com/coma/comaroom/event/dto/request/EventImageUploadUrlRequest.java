package com.coma.comaroom.event.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EventImageUploadUrlRequest(
        @NotBlank String filename,
        @NotBlank String contentType
) {
}
