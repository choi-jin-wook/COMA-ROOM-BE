package com.coma.comaroom.study.dto.response;


/** 모집 목록에 표시할 스터디 정보와 현재 회원의 신청 상태를 담는다. */
public record RecruitingStudyResponse(Long studyId, String studyName, String description,
        long currentMembers, String scheduleDescription, String topic,
        Manager manager, String myJoinRequestStatus) {
    /** 모집 카드에 표시할 스터디장 정보다. */
    public record Manager(Long memberId, String name) {}
}
