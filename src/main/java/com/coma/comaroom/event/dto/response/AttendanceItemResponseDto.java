package com.coma.comaroom.event.dto.response;

import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.member.entity.Major;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AttendanceItemResponseDto {
    private Long memberId;
    private String name;
    private String studentId;
    private Major major;
    private LocalDateTime attendedAt;

    public static AttendanceItemResponseDto from(EventParticipant participant) {
        return AttendanceItemResponseDto.builder()
                .memberId(participant.getParticipantMember().getMemberId())
                .name(participant.getParticipantMember().getName())
                .studentId(participant.getParticipantMember().getStudentId())
                .major(participant.getParticipantMember().getMajor())
                .attendedAt(participant.getCreatedAt())
                .build();
    }
}
