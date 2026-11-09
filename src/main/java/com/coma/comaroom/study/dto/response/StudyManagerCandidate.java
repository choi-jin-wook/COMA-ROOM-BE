package com.coma.comaroom.study.dto.response;

import com.coma.comaroom.member.entity.Member;

/** 관리자에게 노출할 스터디장 후보의 회원 정보를 담는다. */
public record StudyManagerCandidate(Long memberId, String name, String studentId, String major) {
    /** 회원 엔티티에서 후보 목록에 필요한 필드만 추출한다. */
    public static StudyManagerCandidate from(Member member) {
        return new StudyManagerCandidate(member.getMemberId(), member.getName(), member.getStudentId(),
                member.getMajor().getMajor());
    }
}
