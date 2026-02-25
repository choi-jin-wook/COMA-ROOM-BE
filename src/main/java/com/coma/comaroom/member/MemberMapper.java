package com.coma.comaroom.member;

import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.member.dto.request.LeaderboardResponseDto;
import com.coma.comaroom.member.dto.request.MyRankingDto;
import com.coma.comaroom.member.dto.request.RankingItemDto;
import com.coma.comaroom.member.dto.response.*;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.notice.entity.Notice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

@Component
public class MemberMapper {
    public MyRankingDto memberToMyRankingDto(Member currentMember, Long rank) {
        MyRankingDto myRankingDto = MyRankingDto.builder()
                .name(currentMember.getName())
                .major(currentMember.getMajor())
                .xp(currentMember.getXp())
                .rank(rank)
                .build();



        return myRankingDto;
    }

    public LeaderboardResponseDto MyRankingDtoAndMemberListToLeaderboardResponseDto(List<Member> memberList, MyRankingDto myRankingDto) {
// 1. 전체 리스트를 RankingItemDto 리스트로 변환
        List<RankingItemDto> allRankings = IntStream.range(0, memberList.size())
                .mapToObj(i -> {
                    Member m = memberList.get(i);
                    return RankingItemDto.builder()
                            .rank(i + 1)
                            .name(m.getName().length() > 1 ?
                                    m.getName().charAt(0) + "*" + m.getName().substring(m.getName().length() - 1) : m.getName())
                            .major(m.getMajor())
                            .xp(m.getXp().intValue())
                            // myRankingDto에 있는 이름이나 학번 등을 비교하여 본인 여부 확인
                            .isMe(m.getName().equals(myRankingDto.getName()))
                            .build();
                })
                .toList();

        // 2. 상위 3명 추출
        List<RankingItemDto> topThreeRankings = allRankings.stream()
                .limit(3)
                .toList();

        // 3. 최종 결과 조립
        return LeaderboardResponseDto.builder()
                .myRanking(myRankingDto)
                .topThreeRankings(topThreeRankings)
                .allRankings(allRankings)
                .build();
    }

    public MainDashboardResponse createMainDashboardResponse(Member member, Event event, Notice notice) {
        NoticeDto noticeDto = NoticeDto.builder()
                .title(notice.getTitle())
                .content(notice.getContent())
                .date(notice.getCreatedAt().toLocalDate())
                .build();

        LocalDateTime eventDate = event.getEventDate();
        String date = eventDate.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN));
        String dayOfWeek = eventDate.format(DateTimeFormatter.ofPattern("E", Locale.KOREAN));
        String time = eventDate.format(DateTimeFormatter.ofPattern("a h시", Locale.KOREAN));

        UpcomingEventDto upcomingEventDto = UpcomingEventDto.builder()
                .title(event.getTitle())
//                .rewardXp()
                .location(event.getLocation())
                .date(date)
                .dayOfWeek(dayOfWeek)
                .time(time)
                .build();

        Long remainingXp;
        if (member.getXp() >= 50) {
            remainingXp = 0L;
        } else {
            remainingXp = 50 - member.getXp();
        }

        MainDashboardResponse mainDashboardResponse = MainDashboardResponse.builder()
                .userName(member.getName())
                .currentXp(member.getXp())
                .remainingXp(remainingXp)
                .semester("2026년 1학기")
//                .statAttendanceCount()
//                .statEventCount()
//                .myRank()
                .upcomingEvent(upcomingEventDto)
                .notice(noticeDto)
                .build();

        return mainDashboardResponse;
    }

    public ProfileResponseDto createProfileResponseDto(Member member, Long rank, Long attendanceCount, Long eventCount) {
        ProfileResponseDto profileResponseDto = ProfileResponseDto.builder()
                .name(member.getName())
                .major(member.getMajor())
                .studentId(member.getStudentId())
                .ranking(rank)
                .currentXp(member.getXp())
                .joinedDate(member.getCreatedAt().toLocalDate())
                .memberStatus(member.getRole())
                .attendanceCount(attendanceCount)
                .eventCount(eventCount)
                .build();

        return profileResponseDto;
    }

    // 여기서 세 번째 인자인 attendedEventIds를 실제로 '사용'해야 경고가 사라짐
    public AttendanceHistoryDto createAttendanceHistoryDto(Event event, Set<Long> attendedEventIds) {

        // 이 로직이 반드시 들어가야 함!
        boolean isAttended = attendedEventIds.contains(event.getEventId());

        return AttendanceHistoryDto.builder()
                .title(event.getTitle())
                .status(isAttended ? "출석" : "결석") // 여기서 사용됨
                .scheduledDate(event.getEventDate().toString())
                .location(event.getLocation())
                .rewardXp(isAttended ? event.getRewardXp() : 0L)     // 여기서 사용됨
                .build();
    }

    public MainAttendanceResponseDto createMainAttendanceResponseDto(Member member, Long rank, Long eventCount, Long attendanceCount, List<AttendanceHistoryDto> history) {
        Long attendanceRate = 0L;
        if (eventCount > 0) {
            // 2. 100을 먼저 곱해서 소수점 손실 없이 퍼센트 계산
            attendanceRate = (attendanceCount * 100) / eventCount;
        }

        MainAttendanceResponseDto mainAttendanceResponseDto = MainAttendanceResponseDto.builder()
                .totalEventCount(eventCount)
                .attendanceCount(attendanceCount)
                .absenceCount(eventCount - attendanceCount)
                .attendanceRate(attendanceRate)
                .totalEarnedXp(member.getXp())
                .attendanceRank(rank)
                .build();

        return mainAttendanceResponseDto;
    }
}
