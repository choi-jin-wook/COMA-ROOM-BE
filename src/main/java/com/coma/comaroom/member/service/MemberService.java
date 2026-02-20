package com.coma.comaroom.member.service;

import com.coma.comaroom.member.MemberMapper;
import com.coma.comaroom.member.dto.request.LeaderboardResponseDto;
import com.coma.comaroom.member.dto.request.MyRankingDto;
import com.coma.comaroom.member.dto.request.RegisterMemberRequestDto;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
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
}
