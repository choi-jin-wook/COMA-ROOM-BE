package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.study.entity.StudyActivity;

public record StudyActivityResponse(
        Long activityId,
        String activityName,
        Long studyId
) {
    public static StudyActivityResponse from(StudyActivity activity) {
        return new StudyActivityResponse(
                activity.getId(),
                activity.getActivityName(),
                activity.getStudy().getId()
        );
    }
}
