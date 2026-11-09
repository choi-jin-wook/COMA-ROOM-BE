package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.study.entity.StudyMaterial;

/** 주차 상세에 표시할 첨부 자료의 메타데이터를 담는다. */
public record StudyMaterialResponse(Long materialId, String fileName, long sizeBytes) {
    /** 자료 엔티티에서 공개할 메타데이터만 추출한다. */
    public static StudyMaterialResponse from(StudyMaterial material) {
        return new StudyMaterialResponse(material.getId(), material.getFileName(), material.getSizeBytes());
    }
}
