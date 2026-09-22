package com.coma.comaroom.study.service;

import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.dto.response.StudySummary;
import com.coma.comaroom.study.entity.StudyStatus;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudyServiceTest {
    @Test
    void summaryUsesCurrentMembersStudyAggregates() {
        var studies = mock(StudyRepository.class);
        var memberships = mock(StudyMemberRepository.class);
        var security = mock(SecurityUtils.class);
        when(security.getCurrentMember()).thenReturn(Member.builder().memberId(7L).build());
        when(studies.countMine(7L, StudyStatus.ACTIVE)).thenReturn(2L);
        when(studies.countMine(7L, StudyStatus.COMPLETED)).thenReturn(3L);
        when(memberships.totalEarnedXp(7L)).thenReturn(42L);
        var service = new StudyService(studies, memberships, mock(StudyManagerCandidateRepository.class),
                new StudyAccessService(studies, memberships), security);
        assertThat(service.summary()).isEqualTo(new StudySummary(2, 42, 3));
    }

    @Test
    void invalidPaginationIsRejected() {
        assertThatThrownBy(() -> StudyAccessService.page(-1, 20, "id")).isNotNull();
        assertThatThrownBy(() -> StudyAccessService.page(0, 0, "id")).isNotNull();
        assertThatThrownBy(() -> StudyAccessService.page(0, 101, "id")).isNotNull();
        assertThat(StudyAccessService.page(0, 100, "id").getPageSize()).isEqualTo(100);
    }
}
