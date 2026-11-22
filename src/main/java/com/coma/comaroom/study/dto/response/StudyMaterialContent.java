package com.coma.comaroom.study.dto.response;

/** 파일 다운로드 응답에 필요한 이름·크기·바이트를 담는다. */
public record StudyMaterialContent(String fileName, long sizeBytes, byte[] content) {}
