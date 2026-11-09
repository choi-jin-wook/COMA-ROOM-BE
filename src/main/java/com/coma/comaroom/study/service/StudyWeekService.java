package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.request.CreateStudyWeekRequest;
import com.coma.comaroom.study.dto.response.*;
import com.coma.comaroom.study.entity.*;
import com.coma.comaroom.study.repository.*;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.storage.OciStudyMaterialStorage;
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

/** 주차 계획과 첨부 자료의 조회·생성·다운로드를 처리한다. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyWeekService {
    // 자료는 최대 1MB이며 허용 MIME 유형만 OCI에 업로드한다.
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

    /** 저장된 계획이 없는 주차도 포함해 1~16주차를 반환한다. */
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

    /** 멤버 권한을 확인한 뒤 계획과 자료 메타데이터를 조회한다. */
    public StudyWeekResponse detail(Long studyId, int number) {
        StudyAccessService.validateWeek(number);
        Study study = access.getStudy(studyId);
        String role = access.requireMember(study, security.getCurrentMember().getMemberId());
        StudyWeek plan = getPlan(studyId, number);
        return response(study, role, plan, materials.findByPlanIdOrderByIdAsc(plan.getId()));
    }

    /** 계획과 선택적 첨부 파일을 저장하며 트랜잭션 실패 시 OCI 객체를 정리한다. */
    @Transactional
    public StudyWeekResponse create(Long studyId, CreateStudyWeekRequest request, MultipartFile file) {
        if (request == null) throw new BusinessException(StudyError.INVALID_INPUT);
        StudyAccessService.validateWeek(request.weekNumber());
        Study study = access.lockStudy(studyId);
        access.requireLeader(study, security.getCurrentMember().getMemberId());
        if (weeks.existsByStudyIdAndWeekNumber(studyId, request.weekNumber())) {
            throw new BusinessException(StudyError.WEEK_CONFLICT);
        }
        UploadMaterial material = readMaterial(file);
        String objectKey = material == null ? null : "studies/" + studyId + "/weeks/"
                + request.weekNumber() + "/" + UUID.randomUUID();
        // DB 롤백 이후에 외부 저장소의 업로드 객체를 삭제한다.
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
                        .fileName(material.fileName())
                        .sizeBytes(material.content().length).objectKey(objectKey).build()));
            }
            return response(study, "LEADER", plan, savedMaterials);
        } catch (RuntimeException ex) {
            // 트랜잭션 동기화가 없는 직접 호출에서도 업로드 객체가 남지 않게 한다.
            if (objectKey != null && !synchronizedCleanup) storage.deleteAfterRollback(objectKey);
            throw ex;
        }
    }

    /** 현재 회원에게 5분 동안만 유효한 자료 다운로드 URL을 발급한다. */
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

    /** URL 서명과 자료 접근 권한을 확인한 뒤 실제 파일을 읽는다. */
    public StudyMaterialContent download(Long studyId, int number, Long materialId, long expires, String signature) {
        Long memberId = security.getCurrentMember().getMemberId();
        signer.verify(studyId, number, materialId, memberId, expires, signature);
        StudyMaterial material = authorizedMaterial(studyId, number, materialId, memberId);
        return new StudyMaterialContent(material.getFileName(), material.getSizeBytes(),
                storage.download(material.getObjectKey(), material.getSizeBytes()));
    }

    /** 자료 ID가 요청한 스터디와 주차에 속하는지도 함께 검증한다. */
    private StudyMaterial authorizedMaterial(Long studyId, int number, Long materialId, Long memberId) {
        StudyAccessService.validateWeek(number);
        access.requireMember(access.getStudy(studyId), memberId);
        return materials.findByIdAndPlanStudyIdAndPlanWeekNumber(materialId, studyId, number)
                .orElseThrow(() -> new BusinessException(StudyError.MATERIAL_NOT_FOUND));
    }

    /** 해당 주차의 계획이 없으면 주차 미등록 오류를 반환한다. */
    public StudyWeek getPlan(Long studyId, int number) {
        return weeks.findByStudyIdAndWeekNumber(studyId, number)
                .orElseThrow(() -> new BusinessException(StudyError.WEEK_NOT_FOUND));
    }

    /** 엔티티와 자료 목록을 API 응답 형태로 변환한다. */
    private StudyWeekResponse response(Study study, String role, StudyWeek plan, List<StudyMaterial> files) {
        return new StudyWeekResponse(study.getId(), study.getStudyName(), role, plan.getId(),
                plan.getWeekNumber(), plan.getTitle(), plan.getTopic(), plan.getDescription(),
                files.stream().map(StudyMaterialResponse::from).toList());
    }

    /** 검증을 마친 업로드 파일의 메타데이터와 내용을 보관한다. */
    private record UploadMaterial(String fileName, String contentType, byte[] content) {}

    /** 파일 크기·형식·이름을 검사한 뒤 안전한 파일명과 내용을 추출한다. */
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
