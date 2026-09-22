package com.coma.comaroom.study.storage;

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

@Component
@RequiredArgsConstructor
@Slf4j
public class OciStudyMaterialStorage {
    private final ObjectProvider<ObjectStorage> clients;
    private final OciStorageProperties properties;

    public void upload(String key, String contentType, byte[] bytes) {
        try {
            client().putObject(PutObjectRequest.builder().namespaceName(properties.getNamespace())
                    .bucketName(properties.getBucket()).objectName(key).contentType(contentType)
                    .contentLength((long) bytes.length).putObjectBody(new ByteArrayInputStream(bytes)).build());
        } catch (RuntimeException ex) {
            throw new BusinessException(StudyError.UPLOAD_FAILED);
        }
    }

    public void checkExists(String key) {
        try {
            client().headObject(HeadObjectRequest.builder().namespaceName(properties.getNamespace())
                    .bucketName(properties.getBucket()).objectName(key).build());
        } catch (RuntimeException ex) {
            throw readError(ex);
        }
    }

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

    private ObjectStorage client() {
        properties.validate();
        return clients.getObject();
    }

    private BusinessException readError(Exception ex) {
        if (ex instanceof BmcException bmc && bmc.getStatusCode() == 404) {
            return new BusinessException(StudyError.MATERIAL_NOT_FOUND);
        }
        return new BusinessException(StudyError.STORAGE_UNAVAILABLE);
    }
}
