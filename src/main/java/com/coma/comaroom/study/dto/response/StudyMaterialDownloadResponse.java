package com.coma.comaroom.study.dto.response;

import java.time.Instant;

/** 서명된 자료 다운로드 URL과 만료 시각을 반환한다. */
public record StudyMaterialDownloadResponse(String fileName, String downloadUrl, Instant expiresAt) {}
