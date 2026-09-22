package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.study.entity.StudyJoinRequest;

public record StudyJoinRequestResponse(Long joinRequestId, Long studyId, StudyJoinRequest.Status status) {
    public static StudyJoinRequestResponse from(StudyJoinRequest request) {
        return new StudyJoinRequestResponse(request.getId(), request.getStudy().getId(), request.getStatus());
    }
}
