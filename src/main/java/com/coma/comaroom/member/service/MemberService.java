package com.coma.comaroom.member.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventError;
import com.coma.comaroom.event.dto.AskXpRequestDto;
import com.coma.comaroom.event.dto.AskXpResponseDto;
import com.coma.comaroom.event.dto.RecentActivityLogDto;
import com.coma.comaroom.event.dto.XpManagementMainResponseDto;
import com.coma.comaroom.event.entity.*;
import com.coma.comaroom.event.mapper.EventApprovalMapper;
import com.coma.comaroom.event.repository.EventApprovalRepository;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.MemberMapper;
import com.coma.comaroom.member.XpManagementMapper;
import com.coma.comaroom.member.dto.request.LeaderboardResponseDto;
import com.coma.comaroom.member.dto.request.MyRankingDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.response.*;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.notice.exception.NoticeError;
import com.coma.comaroom.notice.repository.NoticeRepository;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteStatus;
import com.coma.comaroom.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final EventRepository eventRepository;
    private final EventParticipateRepository eventParticipateRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;
    private final SecurityUtils securityUtils;
    private final VoteRepository voteRepository;
    private final EventApprovalMapper eventApprovalMapper;
    private final XpManagementMapper xpManagementMapper;
    private final EventApprovalRepository eventApprovalRepository;

    public void registerMember(RegisterMemberRequestDto registerMemberRequestDto) {
        Member member = Member.builder()
                .studentId(registerMemberRequestDto.getStudentId())
                .name(registerMemberRequestDto.getName())
                .role(Role.USER)
                .password(passwordEncoder.encode(registerMemberRequestDto.getPassword()))
                .xp(0L)
                .build();

        memberRepository.saveAndFlush(member);
    }

    public AskXpResponseDto askProvisionXp(AskXpRequestDto xpPetitionRequestDto) {
        Member currentUser = securityUtils.getCurrentMember();
        EventApproval eventApproval = EventApproval.requestXpApproval(currentUser, xpPetitionRequestDto.getProvisionReason(), xpPetitionRequestDto.getProvisionAmount());
        eventApprovalRepository.save(eventApproval);
        AskXpResponseDto xpPetitionResponseDto = new AskXpResponseDto(eventApproval.getId());
        return xpPetitionResponseDto;
    }

    public LeaderboardResponseDto getLeaderboardData() {
        final Integer pageNumber = 0;
        final Integer pageSize = 10;
        Member member = securityUtils.getCurrentMember();
        MyRankingDto myRankingDto = memberMapper.memberToMyRankingDto(member, memberRepository.findRankByMember(member));
        List<Member> memberList = memberRepository.findAllByOrderByXpDescMemberIdAsc(PageRequest.of(pageNumber, pageSize));
        LeaderboardResponseDto leaderboardResponseDto = memberMapper.MyRankingDtoAndMemberListToLeaderboardResponseDto(memberList, myRankingDto);

        return leaderboardResponseDto;
    }

    public MainDashboardResponse getMainDashboard() {
        // 현재 사용자
        Member member = securityUtils.getCurrentMember();

        // 공지 찾기
        Notice notice = noticeRepository.findFirstByOrderByCreatedAtDesc().orElseThrow(() -> new BusinessException(NoticeError.NOTICE_NOT_FOUND));

        Optional<Event> event = eventRepository.findFirstByEventDateAfterOrderByEventDateAsc(LocalDateTime.now());

        // 사용자 순위
        Long rank = memberRepository.findRankByMember(member);

        // 출석횟수, 행사 참여
        Long statAttendanceCount = eventParticipateRepository.countByParticipantMemberAndEvent_EventCategoryNot(member, EventCategory.EVENT);
        Long statEventCount = eventParticipateRepository.countByParticipantMemberAndEvent_EventCategory(member, EventCategory.EVENT);

        // 진행중인 투표
        Optional<Vote> vote = voteRepository.findFirstByVoteStatusOrderByCreatedAtDesc(VoteStatus.IN_PROGRESS);

        MainDashboardResponse mainDashboardResponse = memberMapper.createMainDashboardResponse(member, event, notice, rank, statAttendanceCount, statEventCount, vote);
        return mainDashboardResponse;
    }

    public ProfileResponseDto getMemberProfile() {
        // 현재 사용자
        Member member = securityUtils.getCurrentMember();

        // 사용자의 순위, 행사 이외의 출석 수, 행사 참여 수,
        Long rank = memberRepository.findRankByMember(member);
        Long attendanceCount = eventParticipateRepository.countByParticipantMemberAndEvent_EventCategoryNot(member, EventCategory.EVENT);
        Long eventCount = eventParticipateRepository.countByParticipantMemberAndEvent_EventCategory(member, EventCategory.EVENT);

        // 참여한 행사의 목록
        List<EventParticipant> eventParticipants = eventParticipateRepository.findTop5ByParticipantMemberOrderByEventParticipantIdDesc(member);
        List<RecentActivityDto> recentActivityDtoList = memberMapper.createRecentActivityDto(eventParticipants);

        // mapper을 통한 dto 생성
        ProfileResponseDto profileResponseDto = memberMapper.createProfileResponseDto(member, rank, attendanceCount, eventCount, recentActivityDtoList);
        return profileResponseDto;
    }

    public MainAttendanceResponseDto getMainAttendance() {
        Member member = securityUtils.getCurrentMember();
        Long rank = memberRepository.findRankByMember(member);
        Long eventCount = eventRepository.count();
        Long attendanceCount = eventParticipateRepository.countByParticipantMember(member);

        List<Event> eventList = eventRepository.findAllByOrderByEventDateDesc();


        Set<Long> attendedEventIds = new HashSet<>(
                eventParticipateRepository.findAllEventIdsByMember(member)
        );

        List<AttendanceHistoryDto> history = eventList.stream()
                .map(event -> memberMapper.createAttendanceHistoryDto(event, attendedEventIds))
                // 여기서 날짜 내림차순(최신순) 정렬 추가
                .sorted(Comparator.comparing(AttendanceHistoryDto::getScheduledDate).reversed())
                .collect(Collectors.toList());

        MainAttendanceResponseDto mainAttendanceResponseDto = memberMapper.createMainAttendanceResponseDto(member, rank, eventCount, attendanceCount, history);
        return mainAttendanceResponseDto;
    }

    public XpManagementMainResponseDto getXpManagementMainData(ApprovalStatus status, Long page) {
        // 1. 최근 등록순(DESC) + 상속받은 필드(createdAt) + 5개(size) 설정
        Pageable pageable = PageRequest.of(page.intValue(), 5, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. status null 체크해서 데이터 가져오기
        Page<EventApproval> resultPage = (status == null)
                ? eventApprovalRepository.findAllByOrderByCreatedAtDesc(pageable)
                : eventApprovalRepository.findByApprovalStatusOrderByCreatedAtDesc(status, pageable);

        // 3. 여기서 실제 객체 5개가 최근 순서대로 담김
        // 여기서 꺼내고
        List<EventApproval> eventApprovals = resultPage.getContent();

        // 여기다 쓴다
        List<RecentActivityLogDto> recentActivityLogs =
                eventApprovalMapper.toRecentActivityLogDtos(eventApprovals);

        return xpManagementMapper.toMainDto(
                eventApprovalRepository.countByApprovalStatus(ApprovalStatus.APPROVED),
                eventApprovalRepository.countByApprovalStatus(ApprovalStatus.REJECTED),
                eventApprovalRepository.countByApprovalStatus(ApprovalStatus.PENDING),
                recentActivityLogs
        );
    }

//    public AttendanceMainResponse getAttendanceMainPage() {
//        Member member = securityUtils.getCurrentMember();
//        Long rank = memberRepository.findRankByMember(member);
//
//        Long attendanceCount = eventParticipateRepository.countByParticipantMember(member);
//        Long eventCount = eventRepository.count();
//        List<Event> eventList = eventParticipateRepository.findAllEventsByMember(member);
//
////        List<AttendanceHistoryDto> attendanceHistoryDtoList = memberMapper.createAttendanceHistoryDtoList();
//    }
}
