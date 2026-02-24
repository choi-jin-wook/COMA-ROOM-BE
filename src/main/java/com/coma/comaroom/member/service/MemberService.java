package com.coma.comaroom.member.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.member.MemberMapper;
import com.coma.comaroom.member.dto.request.LeaderboardResponseDto;
import com.coma.comaroom.member.dto.request.MyRankingDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.dto.response.MainDashboardResponse;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.notice.entity.Notice;
import com.coma.comaroom.notice.exception.NoticeError;
import com.coma.comaroom.notice.repository.NoticeRepository;
import com.coma.comaroom.utils.ErrorCode;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;
    private final SecurityUtils securityUtils;

    public void registerMember(RegisterMemberRequestDto registerMemberRequestDto) {

        Member member = Member.builder()
                .studentId(registerMemberRequestDto.getStudentId())
                .name(registerMemberRequestDto.getName())
                .role(Role.USER)
                .password(passwordEncoder.encode(registerMemberRequestDto.getPassword()))
                .xp(0L)
                .build();


        memberRepository.save(member);
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
        Member member = securityUtils.getCurrentMember();
        Notice notice = noticeRepository.findFirstByOrderByCreatedAtDesc().orElseThrow(() -> new BusinessException(NoticeError.NOTICE_NOT_FOUND));
        Optional<Event> event = eventRepository.findFirstByEventDateAfterOrderByEventDateAsc(LocalDateTime.now());


        MainDashboardResponse mainDashboardResponse = memberMapper.createMainDashboardResponse(member, event, notice);
        return mainDashboardResponse;
    }
}
