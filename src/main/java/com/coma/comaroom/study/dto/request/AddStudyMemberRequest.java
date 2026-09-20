package com.coma.comaroom.study.dto.request;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.entity.Study;
import com.coma.comaroom.study.entity.StudyMember;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddStudyMemberRequest {
    private Long memberId;

    public StudyMember toEntity(Study study, Member member) {
        return StudyMember.builder()
                .study(study)
                .member(member)
                .build();
    }
}
