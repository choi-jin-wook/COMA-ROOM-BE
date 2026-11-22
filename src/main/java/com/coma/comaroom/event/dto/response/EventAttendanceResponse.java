package com.coma.comaroom.event.dto.response;

import java.util.List;

public record EventAttendanceResponse(
        Long eventId,
        String title,
        List<AttendanceItemResponseDto> attendances
) {
}
