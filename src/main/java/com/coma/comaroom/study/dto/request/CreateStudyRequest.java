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

/** 새 스터디 이름, 스터디장 회원 ID, 주제를 받는다. */
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

    @Size(max = 255)
    private String topic;

    public CreateStudyRequest(String studyName, Long managerId) {
        this(studyName, managerId, null);
    }

    /** 확인된 스터디장과 공백을 정리한 이름으로 엔티티를 만든다. */
    public Study toEntity(Member studyManager) {
        return Study.builder()
                .studyName(studyName.strip())
                .studyManager(studyManager)
                .topic(topic == null ? null : topic.strip())
                .build();
    }
}
