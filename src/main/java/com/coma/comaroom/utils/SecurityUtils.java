package com.coma.comaroom.utils;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.auth.AuthError;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.auth.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public Member getCurrentMember() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BusinessException(AuthError.MEMBER_NOT_FOUND);
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return null;
        }

        return ((CustomUserDetails) principal).getMember();
    }
}
