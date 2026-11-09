package com.coma.comaroom.study.dto.request;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.entity.Study;
import com.coma.comaroom.study.entity.StudyMember;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 관리자가 스터디에 추가할 회원 ID를 전달한다. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddStudyMemberRequest {
    private Long memberId;

    /** 조회한 스터디와 회원으로 멤버십 엔티티를 만든다. */
    public StudyMember toEntity(Study study, Member member) {
        return StudyMember.builder()
                .study(study)
                .member(member)
                .build();
    }
}
