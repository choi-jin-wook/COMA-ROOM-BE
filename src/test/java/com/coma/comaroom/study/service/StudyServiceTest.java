package com.coma.comaroom.study.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** 스터디 목록 페이지 입력 범위를 확인한다. */
class StudyServiceTest {
    @Test
    void invalidPaginationIsRejected() {
        assertThatThrownBy(() -> StudyAccessService.page(-1, 20, "id")).isNotNull();
        assertThatThrownBy(() -> StudyAccessService.page(0, 0, "id")).isNotNull();
        assertThatThrownBy(() -> StudyAccessService.page(0, 101, "id")).isNotNull();
        assertThat(StudyAccessService.page(0, 100, "id").getPageSize()).isEqualTo(100);
    }
}
