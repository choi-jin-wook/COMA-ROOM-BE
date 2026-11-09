package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.study.entity.StudyJoinRequest;

/** 참여 신청 결과와 현재 처리 상태를 반환한다. */
public record StudyJoinRequestResponse(Long joinRequestId, Long studyId, StudyJoinRequest.Status status) {
    /** 저장된 신청 엔티티를 API 응답으로 변환한다. */
    public static StudyJoinRequestResponse from(StudyJoinRequest request) {
        return new StudyJoinRequestResponse(request.getId(), request.getStudy().getId(), request.getStatus());
    }
}
