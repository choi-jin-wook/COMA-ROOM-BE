package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.request.CreateStudyWeekRequest;
import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.entity.*;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.study.storage.OciStudyMaterialStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyWeekService {
    private static final long MAX_FILE_BYTES = 1024 * 1024;
    private static final Set<String> CONTENT_TYPES = Set.of("application/pdf", "text/plain", "text/csv",
            "image/png", "image/jpeg", "application/zip",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private final StudyAccessService access;
    private final StudyWeekRepository weeks;
    private final StudyMaterialRepository materials;
    private final SecurityUtils security;
    private final StudyDownloadSigner signer;
    private final OciStudyMaterialStorage storage;

    public StudyWeeksResponse weeks(Long studyId) {
        Study study = access.getStudy(studyId);
        String role = access.requireMember(study, security.getCurrentMember().getMemberId());
        Map<Integer, StudyWeek> plans = weeks.findByStudyIdOrderByWeekNumberAsc(studyId).stream()
                .collect(Collectors.toMap(StudyWeek::getWeekNumber, plan -> plan));
        return new StudyWeeksResponse(studyId, study.getStudyName(), role,
                IntStream.rangeClosed(1, 16).mapToObj(number -> {
                    StudyWeek plan = plans.get(number);
                    return new StudyWeeksResponse.WeekSummary(number,
                            plan == null ? null : plan.getId(), plan == null ? null : plan.getTitle());
                }).toList());
    }

    public StudyWeekResponse detail(Long studyId, int number) {
        StudyAccessService.validateWeek(number);
        Study study = access.getStudy(studyId);
        String role = access.requireMember(study, security.getCurrentMember().getMemberId());
        StudyWeek plan = getPlan(studyId, number);
        return response(study, role, plan, materials.findByPlanIdOrderByIdAsc(plan.getId()));
    }

    @Transactional
    public StudyWeekResponse create(Long studyId, CreateStudyWeekRequest request, MultipartFile file) {
        StudyAccessService.validateWeek(request.weekNumber());
        Study study = access.lockStudy(studyId);
        access.requireLeader(study, security.getCurrentMember().getMemberId());
        if (weeks.existsByStudyIdAndWeekNumber(studyId, request.weekNumber())) {
            throw new BusinessException(StudyError.WEEK_CONFLICT);
        }
        UploadMaterial material = readMaterial(file);
        String objectKey = material == null ? null : "studies/" + studyId + "/weeks/"
                + request.weekNumber() + "/" + UUID.randomUUID();
        boolean synchronizedCleanup = objectKey != null && TransactionSynchronizationManager.isSynchronizationActive();
        if (synchronizedCleanup) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) storage.deleteAfterRollback(objectKey);
                }
            });
        }
        try {
            if (material != null) storage.upload(objectKey, material.contentType(), material.content());
            StudyWeek plan = weeks.save(StudyWeek.builder().study(study).weekNumber(request.weekNumber())
                    .title(request.title().strip()).build());
            List<StudyMaterial> savedMaterials = List.of();
            if (material != null) {
                savedMaterials = List.of(materials.save(StudyMaterial.builder().plan(plan)
                        .fileName(material.fileName()).contentType(material.contentType())
                        .sizeBytes(material.content().length).objectKey(objectKey).build()));
            }
            return response(study, "LEADER", plan, savedMaterials);
        } catch (RuntimeException ex) {
            if (objectKey != null && !synchronizedCleanup) storage.deleteAfterRollback(objectKey);
            throw ex;
        }
    }

    public StudyMaterialDownloadResponse downloadUrl(Long studyId, int number, Long materialId, String baseUrl) {
        Long memberId = security.getCurrentMember().getMemberId();
        StudyMaterial material = authorizedMaterial(studyId, number, materialId, memberId);
        storage.checkExists(material.getObjectKey());
        Instant expires = Instant.ofEpochSecond(Instant.now().getEpochSecond() + 300);
        String signature = signer.sign(studyId, number, materialId, memberId, expires.getEpochSecond());
        String url = baseUrl + "/api/studies/" + studyId + "/weeks/" + number + "/materials/" + materialId
                + "/content?expires=" + expires.getEpochSecond() + "&signature=" + signature;
        return new StudyMaterialDownloadResponse(material.getFileName(), url, expires);
    }

    public StudyMaterialContent download(Long studyId, int number, Long materialId, long expires, String signature) {
        Long memberId = security.getCurrentMember().getMemberId();
        signer.verify(studyId, number, materialId, memberId, expires, signature);
        StudyMaterial material = authorizedMaterial(studyId, number, materialId, memberId);
        return new StudyMaterialContent(material.getFileName(), material.getSizeBytes(),
                storage.download(material.getObjectKey(), material.getSizeBytes()));
    }

    private StudyMaterial authorizedMaterial(Long studyId, int number, Long materialId, Long memberId) {
        StudyAccessService.validateWeek(number);
        access.requireMember(access.getStudy(studyId), memberId);
        return materials.findByIdAndPlanStudyIdAndPlanWeekNumber(materialId, studyId, number)
                .orElseThrow(() -> new BusinessException(StudyError.MATERIAL_NOT_FOUND));
    }

    public StudyWeek getPlan(Long studyId, int number) {
        return weeks.findByStudyIdAndWeekNumber(studyId, number)
                .orElseThrow(() -> new BusinessException(StudyError.WEEK_NOT_FOUND));
    }

    private StudyWeekResponse response(Study study, String role, StudyWeek plan, List<StudyMaterial> files) {
        return new StudyWeekResponse(study.getId(), study.getStudyName(), role, plan.getId(),
                plan.getWeekNumber(), plan.getTitle(), plan.getTopic(), plan.getDescription(),
                files.stream().map(StudyMaterialResponse::from).toList());
    }

    private record UploadMaterial(String fileName, String contentType, byte[] content) {}

    private UploadMaterial readMaterial(MultipartFile file) {
        if (file == null) return null;
        if (file.getSize() > MAX_FILE_BYTES) throw new BusinessException(StudyError.FILE_TOO_LARGE);
        if (file.isEmpty()) throw new BusinessException(StudyError.INVALID_INPUT);
        if (file.getContentType() == null || !CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException(StudyError.UNSUPPORTED_FILE);
        }
        String name = file.getOriginalFilename();
        if (name == null) throw new BusinessException(StudyError.INVALID_INPUT);
        name = name.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).strip();
        if (name.isBlank() || name.length() > 255 || name.chars().anyMatch(Character::isISOControl)) {
            throw new BusinessException(StudyError.INVALID_INPUT);
        }
        try {
            return new UploadMaterial(name, file.getContentType(), file.getBytes());
        } catch (IOException ex) {
            throw new BusinessException(StudyError.UPLOAD_FAILED);
        }
    }
}
