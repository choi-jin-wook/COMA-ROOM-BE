package com.coma.comaroom.member.dto.request;

import com.coma.comaroom.member.entity.Major;
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
}
