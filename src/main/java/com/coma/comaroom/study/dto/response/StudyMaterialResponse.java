package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.study.entity.StudyMaterial;

public record StudyMaterialResponse(Long materialId, String fileName, String contentType, long sizeBytes) {
    public static StudyMaterialResponse from(StudyMaterial material) {
        return new StudyMaterialResponse(material.getId(), material.getFileName(),
                material.getContentType(), material.getSizeBytes());
    }
}
