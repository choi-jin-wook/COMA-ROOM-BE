package com.coma.comaroom.storage;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.study.StudyError;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/** OCI Object Storage의 자료 업로드·조회·다운로드·롤백 정리를 감싼다. */
@Component
@RequiredArgsConstructor
@Slf4j
public class OciStudyMaterialStorage {
    private final ObjectProvider<ObjectStorage> clients;
    private final OciStorageProperties properties;

    /** 검증된 파일 바이트를 지정한 객체 키로 업로드한다. */
    public void upload(String key, String contentType, byte[] bytes) {
        try {
            client().putObject(PutObjectRequest.builder().namespaceName(properties.getNamespace())
                    .bucketName(properties.getBucket()).objectName(key).contentType(contentType)
                    .contentLength((long) bytes.length).putObjectBody(new ByteArrayInputStream(bytes)).build());
        } catch (RuntimeException ex) {
            throw new BusinessException(StudyError.UPLOAD_FAILED);
        }
    }

    /** 다운로드 URL 발급 전에 객체가 실제로 존재하는지 확인한다. */
    public void checkExists(String key) {
        try {
            client().headObject(HeadObjectRequest.builder().namespaceName(properties.getNamespace())
                    .bucketName(properties.getBucket()).objectName(key).build());
        } catch (RuntimeException ex) {
            throw readError(ex);
        }
    }

    /** 저장된 크기보다 많은 데이터를 읽지 않고 응답 크기도 대조한다. */
    public byte[] download(String key, long expectedSize) {
        try {
            if (expectedSize < 0 || expectedSize > 1024 * 1024) {
                throw new BusinessException(StudyError.STORAGE_UNAVAILABLE);
            }
            var response = client().getObject(GetObjectRequest.builder().namespaceName(properties.getNamespace())
                    .bucketName(properties.getBucket()).objectName(key).build());
            try (InputStream stream = response.getInputStream()) {
                byte[] bytes = stream.readNBytes((int) expectedSize + 1);
                if (bytes.length != expectedSize) throw new BusinessException(StudyError.STORAGE_UNAVAILABLE);
                return bytes;
            }
        } catch (IOException | RuntimeException ex) {
            throw readError(ex);
        }
    }

    /** DB 롤백 뒤 업로드 객체를 삭제하고 정리 실패는 기록한다. */
    public void deleteAfterRollback(String key) {
        try {
            client().deleteObject(DeleteObjectRequest.builder().namespaceName(properties.getNamespace())
                    .bucketName(properties.getBucket()).objectName(key).build());
        } catch (RuntimeException ex) {
            if (ex instanceof BmcException bmc && bmc.getStatusCode() == 404) return;
            // 삭제 실패로 원래 트랜잭션 오류를 가리지 않는다. 객체 키로 운영자가 재시도할 수 있다.
            log.error("스터디 자료 롤백 객체 삭제 실패: objectKey={}, errorType={}", key, ex.getClass().getSimpleName());
        }
    }

    /** 설정을 확인한 뒤 지연 생성된 OCI 클라이언트를 얻는다. */
    private ObjectStorage client() {
        properties.validate();
        return clients.getObject();
    }

    /** OCI의 객체 없음과 그 밖의 저장소 오류를 도메인 오류로 구분한다. */
    private BusinessException readError(Exception ex) {
        if (ex instanceof BmcException bmc && bmc.getStatusCode() == 404) {
            return new BusinessException(StudyError.MATERIAL_NOT_FOUND);
        }
        return new BusinessException(StudyError.STORAGE_UNAVAILABLE);
    }
}
