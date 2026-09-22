package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.member.entity.Member;

public record StudyManagerCandidate(Long memberId, String name, String studentId, String major) {
    public static StudyManagerCandidate from(Member member) {
        return new StudyManagerCandidate(member.getMemberId(), member.getName(), member.getStudentId(),
                member.getMajor().getMajor());
    }
}
