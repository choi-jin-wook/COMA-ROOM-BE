package com.coma.comaroom.event.entity;

import lombok.Getter;

@Getter
public enum EventCategory {
    REGULAR_MEETING(3),  // 정기회의 출석
    EVENT(5),            // 행사
    COMPETITION(10),     // 대회
    STUDY(5),            // 스터디
    STAFF(2),            // 스태프
    MT(20),              // MT
    DINNER(3),           // 회식
    VOTE(2),             // 투표
    MANITO(3);           // 마니또

    private final Integer defaultXp; // 각 카테고리별 기본 부여 XP

    EventCategory(Integer defaultXp) {
        this.defaultXp = defaultXp;
    }
}