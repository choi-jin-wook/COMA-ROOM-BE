package com.coma.comaroom.event.dto.request;

import java.util.List;

public record EventPostRequest(
            String title,
            Long eventId,
            List<String> photoUrls
    ) {}

