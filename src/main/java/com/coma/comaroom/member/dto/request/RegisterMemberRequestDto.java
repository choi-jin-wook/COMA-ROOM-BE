package com.coma.comaroom.member.dto.request;

import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMemberRequestDto {
    @NotBlank
    private String studentId;

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 8, max = 64)
    private String password;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private Major major;

    /**
     * 비밀번호는 서비스에서 인코딩한 값을 넘겨받는다. (DTO가 PasswordEncoder를 알지 않도록)
     * 가입 시 역할은 항상 USER로 고정한다.
     */
    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .studentId(studentId)
                .name(name)
                .password(encodedPassword)
                .phoneNumber(phoneNumber)
                .role(Role.USER)
                .xp(0L)
                .major(major)
                .build();
    }
}
