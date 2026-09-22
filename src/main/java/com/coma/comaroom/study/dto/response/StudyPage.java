package com.coma.comaroom.study.dto.response;

import org.springframework.data.domain.Page;
import java.util.List;

public record StudyPage<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
    public static <T> StudyPage<T> from(Page<T> page) {
        return new StudyPage<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
