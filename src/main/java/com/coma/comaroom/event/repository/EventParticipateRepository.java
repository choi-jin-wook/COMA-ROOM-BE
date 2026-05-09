package com.coma.comaroom.event.repository;

import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.member.entity.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventParticipateRepository extends JpaRepository<EventParticipant, Long> {
    /**
     * 특정 회원이 참여한 행사 중 카테고리가 일치하는 횟수 조회
     * (예: EVENT 카테고리 참여 횟수)
     */
    Long countByParticipantMemberAndEvent_EventCategory(Member member, EventCategory category);

    /**
     * 특정 회원이 참여한 행사 중 특정 카테고리가 아닌 횟수 조회
     * (예: EVENT가 아닌 모든 참여 횟수)
     */
    Long countByParticipantMemberAndEvent_EventCategoryNot(Member member, EventCategory category);

    // 사용자 출석 수
    Long countByParticipantMember(Member member);

    boolean existsByParticipantMemberAndEvent(Member member, Event event);

    // 특정 멤버가 참여한 이벤트들의 ID 리스트만 조회
    @Query("SELECT ep.event.id FROM EventParticipant ep WHERE ep.participantMember = :member")
    List<Long> findAllEventIdsByMember(@Param("member") Member member);

    // 특정 멤버가 참여한 이벤트들의 리스트 조회
    @Query("SELECT ep.event FROM EventParticipant ep " +
            "JOIN ep.event " +
            "WHERE ep.participantMember = :member")
    List<Event> findAllEventsByMember(@Param("member") Member member);

    List<EventParticipant> findTop5ByParticipantMemberOrderByEventParticipantIdDesc(Member member);

    // 사용자 XP 내역: 특정 회원의 전체 참여 목록 (최신순)
    List<EventParticipant> findByParticipantMemberOrderByEventParticipantIdDesc(Member member);

    // 카테고리별 XP 합산용
    List<EventParticipant> findByParticipantMemberAndEvent_EventCategory(Member member, EventCategory category);

    // 특정 이벤트의 전체 출석 명단
    List<EventParticipant> findByEvent(Event event);

    // 특정 이벤트 + 특정 멤버의 출석 기록 조회 (출석 조정용)
    java.util.Optional<EventParticipant> findByEventAndParticipantMember(Event event, Member member);
}
