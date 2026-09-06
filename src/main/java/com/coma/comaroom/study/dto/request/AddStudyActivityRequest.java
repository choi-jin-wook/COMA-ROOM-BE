package com.coma.comaroom.study.dto.request;

import com.coma.comaroom.study.entity.Study;
import com.coma.comaroom.study.entity.StudyActivity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddStudyActivityRequest {
    private String activityName;

    public StudyActivity toEntity(Study study) {
        return StudyActivity.builder()
                .activityName(activityName)
                .study(study)
                .build();
    }
}
