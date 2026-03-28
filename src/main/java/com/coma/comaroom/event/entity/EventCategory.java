package com.coma.comaroom.event.entity;

import lombok.Getter;

@Getter
public enum EventCategory {
    REGULAR_MEETING(3L), // 정기회의
    EVENT(5L),           // 행사
    STUDY(5L),           // 스터디
    LAB(5L),             // 랩실 활동
    STAFF(2L);           // 스태프

    private final Long defaultXp; // Integer에서 Long으로 변경

    // 생성자 파라미터 타입도 Long으로 변경
    EventCategory(Long defaultXp) {
        this.defaultXp = defaultXp;
    }
}