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

    @NotNull
    private Major major;

    @NotNull
    private Role role;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .studentId(studentId)
                .name(name)
                .password(encodedPassword)
                .role(role)
                .xp(0L)
                .major(major)
                .build();
    }
}
