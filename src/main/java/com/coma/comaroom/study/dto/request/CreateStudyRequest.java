package com.coma.comaroom.study.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudyRequest {
    private String studyName;
    private Long managerId;
}
