package com.coma.comaroom.event.entity;

import lombok.Getter;

@Getter
public enum EventCategory {
    REGULAR_MEETING(3),  // 정기회의 출석
    EVENT(5),            // 행사
    STUDY(5),            // 스터디
    LAB(5),
    STAFF(2);            // 스태프
    private final Integer defaultXp; // 각 카테고리별 기본 부여 XP

    EventCategory(Integer defaultXp) {
        this.defaultXp = defaultXp;
    }
}