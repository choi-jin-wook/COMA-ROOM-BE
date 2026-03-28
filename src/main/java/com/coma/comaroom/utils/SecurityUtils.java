package com.coma.comaroom.utils;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.AuthError;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.auth.CustomUserDetails;
import com.coma.comaroom.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final MemberRepository memberRepository;

    public Member getCurrentMember() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BusinessException(AuthError.MEMBER_NOT_FOUND);
        }

        Long memberId = userDetails.getMemberId();

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(AuthError.MEMBER_NOT_FOUND));
    }
}