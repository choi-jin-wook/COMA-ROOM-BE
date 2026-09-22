package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.request.CreateStudyWeekRequest;
import com.coma.comaroom.study.entity.*;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.study.storage.OciStudyMaterialStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class StudyWeekServiceTest {
    private final StudyRepository studies = mock(StudyRepository.class);
    private final StudyMemberRepository memberships = mock(StudyMemberRepository.class);
    private final StudyWeekRepository weeks = mock(StudyWeekRepository.class);
    private final StudyMaterialRepository materials = mock(StudyMaterialRepository.class);
    private final SecurityUtils security = mock(SecurityUtils.class);
    private final StudyAccessService access = new StudyAccessService(studies, memberships);
    private final OciStudyMaterialStorage storage = mock(OciStudyMaterialStorage.class);
    private final StudyWeekService service = new StudyWeekService(access, weeks, materials, security,
            new StudyDownloadSigner("test-study-secret"), storage);
    private final Member leader = Member.builder().memberId(1L).build();
    private final Study study = Study.builder().id(10L).studyName("Study").studyManager(leader).build();

    @BeforeEach
    void setup() {
        when(security.getCurrentMember()).thenReturn(leader);
        when(studies.findById(10L)).thenReturn(Optional.of(study));
        when(studies.findForUpdate(10L)).thenReturn(Optional.of(study));
    }

    @Test
    void missingPlansAreNullAndAllSixteenWeeksAreReturnedInOrder() {
        when(weeks.findByStudyIdOrderByWeekNumberAsc(10L)).thenReturn(List.of(
                StudyWeek.builder().id(30L).weekNumber(3).title("Third").study(study).build()));
        var response = service.weeks(10L);
        assertThat(response.myRole()).isEqualTo("LEADER");
        assertThat(response.weeks()).hasSize(16);
        assertThat(response.weeks().getFirst().weekNumber()).isEqualTo(1);
        assertThat(response.weeks().getFirst().planId()).isNull();
        assertThat(response.weeks().get(2).title()).isEqualTo("Third");
        assertThat(response.weeks().getLast().weekNumber()).isEqualTo(16);
    }

    @Test
    void outsiderCannotReadAndMemberCannotCreate() {
        when(security.getCurrentMember()).thenReturn(Member.builder().memberId(2L).build());
        assertThatThrownBy(() -> service.weeks(10L)).isInstanceOf(BusinessException.class)
                .hasMessage(StudyError.ACCESS_DENIED.getMessage());
        when(memberships.existsByStudyIdAndMemberMemberId(10L, 2L)).thenReturn(true);
        assertThat(service.weeks(10L).myRole()).isEqualTo("MEMBER");
        assertThatThrownBy(() -> service.create(10L, new CreateStudyWeekRequest(1, "Title"), null))
                .hasMessage(StudyError.ACCESS_DENIED.getMessage());
        verify(weeks, never()).save(any());
    }

    @Test
    void absentPlanAndInvalidWeekAreNotEmptySuccesses() {
        assertThatThrownBy(() -> service.detail(10L, 1)).hasMessage(StudyError.WEEK_NOT_FOUND.getMessage());
        assertThatThrownBy(() -> service.detail(10L, 17)).hasMessage(StudyError.INVALID_INPUT.getMessage());
    }

    @Test
    void duplicatePlanIsConflict() {
        when(weeks.existsByStudyIdAndWeekNumber(10L, 1)).thenReturn(true);
        assertThatThrownBy(() -> service.create(10L, new CreateStudyWeekRequest(1, "Title"), null))
                .hasMessage(StudyError.WEEK_CONFLICT.getMessage());
        verify(weeks, never()).save(any());
    }

    @Test
    void optionalFileAndTitleAreSaved() {
        when(weeks.save(any())).thenAnswer(call -> {
            StudyWeek plan = call.getArgument(0);
            return StudyWeek.builder().id(20L).study(study).weekNumber(plan.getWeekNumber())
                    .title(plan.getTitle()).build();
        });
        when(materials.save(any())).thenAnswer(call -> call.getArgument(0));
        var response = service.create(10L, new CreateStudyWeekRequest(1, "  Plan  "),
                new MockMultipartFile("file", "../notes.txt", "text/plain", "notes".getBytes()));
        assertThat(response.title()).isEqualTo("Plan");
        assertThat(response.materials()).singleElement().satisfies(material -> {
            assertThat(material.fileName()).isEqualTo("notes.txt");
            assertThat(material.sizeBytes()).isEqualTo(5);
        });
    }

    @Test
    void uploadFailureDoesNotSavePlan() throws IOException {
        var file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getOriginalFilename()).thenReturn("notes.txt");
        when(file.getBytes()).thenThrow(new IOException("storage failed"));
        assertThatThrownBy(() -> service.create(10L, new CreateStudyWeekRequest(1, "Title"), file))
                .hasMessage(StudyError.UPLOAD_FAILED.getMessage());
        verify(weeks, never()).save(any());
    }

    @Test
    void rejectsUnsupportedAndOversizedFiles() {
        assertThatThrownBy(() -> service.create(10L, new CreateStudyWeekRequest(1, "Title"),
                new MockMultipartFile("file", "page.html", "text/html", "text".getBytes())))
                .hasMessage(StudyError.UNSUPPORTED_FILE.getMessage());
        assertThatThrownBy(() -> service.create(10L, new CreateStudyWeekRequest(1, "Title"),
                new MockMultipartFile("file", "large.pdf", "application/pdf", new byte[1024 * 1024 + 1])))
                .hasMessage(StudyError.FILE_TOO_LARGE.getMessage());
        verify(weeks, never()).save(any());
    }

    @Test
    void foreignMaterialCannotIssueLink() {
        assertThatThrownBy(() -> service.downloadUrl(10L, 1, 99L, "https://example.test"))
                .hasMessage(StudyError.MATERIAL_NOT_FOUND.getMessage());
        verify(materials).findByIdAndPlanStudyIdAndPlanWeekNumber(99L, 10L, 1);
    }

    @Test
    void validLinkStillRequiresCurrentMembership() {
        long expires = java.time.Instant.now().plusSeconds(300).getEpochSecond();
        var signer = new StudyDownloadSigner("test-study-secret");
        String signature = signer.sign(10L, 1, 30L, 2L, expires);
        when(security.getCurrentMember()).thenReturn(Member.builder().memberId(2L).build());
        assertThatThrownBy(() -> service.download(10L, 1, 30L, expires, signature))
                .hasMessage(StudyError.ACCESS_DENIED.getMessage());
        verifyNoInteractions(storage);
    }

    @Test
    void ociUploadFailureDoesNotPersistPlan() {
        doThrow(new BusinessException(StudyError.UPLOAD_FAILED)).when(storage).upload(anyString(), anyString(), any());
        assertThatThrownBy(() -> service.create(10L, new CreateStudyWeekRequest(1, "Title"),
                new MockMultipartFile("file", "notes.txt", "text/plain", "notes".getBytes())))
                .hasMessage(StudyError.UPLOAD_FAILED.getMessage());
        verify(weeks, never()).save(any());
        verify(storage).deleteAfterRollback(startsWith("studies/10/weeks/1/"));
    }

    @Test
    void databaseRollbackDeletesObjectButCommitKeepsIt() {
        when(weeks.save(any())).thenAnswer(call -> call.getArgument(0));
        when(materials.save(any())).thenAnswer(call -> call.getArgument(0));
        for (int status : new int[]{TransactionSynchronization.STATUS_ROLLED_BACK, TransactionSynchronization.STATUS_COMMITTED}) {
            clearInvocations(storage);
            TransactionSynchronizationManager.initSynchronization();
            try {
                service.create(10L, new CreateStudyWeekRequest(1, "Title"),
                        new MockMultipartFile("file", "notes.txt", "text/plain", "notes".getBytes()));
                verify(storage, never()).deleteAfterRollback(anyString());
                TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCompletion(status));
                verify(storage, times(status == TransactionSynchronization.STATUS_ROLLED_BACK ? 1 : 0))
                        .deleteAfterRollback(anyString());
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
            }
        }
    }

    @Test
    void storesOnlyObjectKeyAndDownloadsOciBytesAfterAuthorization() {
        var plan = StudyWeek.builder().id(20L).study(study).build();
        var material = StudyMaterial.builder().id(30L).plan(plan).fileName("notes.txt")
                .sizeBytes(5).objectKey("studies/10/weeks/1/key").build();
        when(materials.findByIdAndPlanStudyIdAndPlanWeekNumber(30L, 10L, 1)).thenReturn(Optional.of(material));
        service.downloadUrl(10L, 1, 30L, "https://example.test");
        verify(storage).checkExists(material.getObjectKey());
        when(storage.download(material.getObjectKey(), 5)).thenReturn("notes".getBytes());
        long expires = java.time.Instant.now().plusSeconds(300).getEpochSecond();
        String signature = new StudyDownloadSigner("test-study-secret").sign(10L, 1, 30L, 1L, expires);
        assertThat(service.download(10L, 1, 30L, expires, signature).content()).isEqualTo("notes".getBytes());
    }
}
