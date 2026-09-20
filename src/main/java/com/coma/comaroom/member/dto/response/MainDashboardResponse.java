package com.coma.comaroom.member.dto.response;

import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.vote.entity.Vote;
import lombok.Getter;
import lombok.Builder;

import java.util.Optional;

@Getter
@Builder
public class MainDashboardResponse {
    // 1. 상단 사용자 카드 정보
    private String semester;         // "2026년 1학기"
    private String userName;         // "최진욱"
    private Long currentXp;           // 18
    private Long remainingXp;         // 32

    // 2. 중앙 통계 지표 (요청하신 항목 제외)
    private Long statAttendanceCount; // 출석 횟수: 6회
    private Long statEventCount;      // 행사 참여: 2회

    // 3. 나의 순위 섹션
    private Long myRank;              // 12

    // 4. 각 섹션별 단일 데이터 (리스트 제거)
    private UpcomingEventDto upcomingEvent;
    private NoticeDto notice;
    private VoteDto votePoll;

    private static final Long XP_GOAL = 50L;

    public static MainDashboardResponse of(
            Member member,
            Optional<Event> event,
            Notice notice,
            Long rank,
            Long statAttendanceCount,
            Long statEventCount,
            Optional<Vote> vote
    ) {
        Long remainingXp = member.getXp() >= XP_GOAL ? 0L : XP_GOAL - member.getXp();

        return MainDashboardResponse.builder()
                .userName(member.getName())
                .currentXp(member.getXp())
                .remainingXp(remainingXp)
                .semester("2026년 1학기")
                .statAttendanceCount(statAttendanceCount)
                .statEventCount(statEventCount)
                .myRank(rank)
                .upcomingEvent(event.map(UpcomingEventDto::from).orElse(null))
                .notice(notice == null ? null : NoticeDto.from(notice))
                .votePoll(vote.map(VoteDto::from).orElse(null))
                .build();
    }
}
