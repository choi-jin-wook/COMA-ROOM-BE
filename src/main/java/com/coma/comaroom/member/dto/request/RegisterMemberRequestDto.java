package com.coma.comaroom.member.dto.request;

import com.coma.comaroom.member.entity.Major;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMemberRequestDto {
    private String studentId;
    private String name;
    private String password;
    private Major major;
}
