package com.coma.comaroom.study.dto.response;

import java.time.Instant;

public record StudyMaterialDownloadResponse(String fileName, String downloadUrl, Instant expiresAt) {}
