package com.coma.comaroom.study.dto.request;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.entity.Study;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudyRequest {
    private String studyName;
    private Long managerId;

    public Study toEntity(Member studyManager) {
        return Study.builder()
                .studyName(studyName)
                .studyManager(studyManager)
                .build();
    }
}
