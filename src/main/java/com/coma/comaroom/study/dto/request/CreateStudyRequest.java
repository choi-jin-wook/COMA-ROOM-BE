package com.coma.comaroom.study.dto.request;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.entity.Study;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudyRequest {
    @NotBlank
    @Size(max = 255)
    private String studyName;
    @NotNull
    @Positive
    private Long managerId;

    public Study toEntity(Member studyManager) {
        return Study.builder()
                .studyName(studyName.strip())
                .studyManager(studyManager)
                .build();
    }
}
