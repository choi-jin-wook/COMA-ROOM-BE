package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.study.entity.StudyLevel;
import java.util.List;

public record RecruitingStudyResponse(Long studyId, String studyName, String description,
        long currentMembers, Integer maxMembers, String scheduleDescription, StudyLevel level,
        List<String> tags, Manager manager, String myJoinRequestStatus) {
    public record Manager(Long memberId, String name) {}
}
