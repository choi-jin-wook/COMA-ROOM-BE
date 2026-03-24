package com.coma.comaroom.auth;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long memberId;
    private final String studentId;
    private final String password;
    private final Role role;
    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member; // 2. 필드에 할당
        this.memberId = member.getMemberId();
        this.studentId = member.getStudentId();
        this.password = member.getPassword();
        this.role = member.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return studentId; // 3. 로그인 ID인 학번을 반환
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}