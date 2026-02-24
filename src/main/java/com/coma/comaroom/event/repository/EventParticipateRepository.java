package com.coma.comaroom.event.repository;

import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.entity.EventPost;
import com.coma.comaroom.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

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

}
