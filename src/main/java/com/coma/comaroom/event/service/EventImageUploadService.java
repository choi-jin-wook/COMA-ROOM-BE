package com.coma.comaroom.event.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.event.EventPostError;
import com.coma.comaroom.event.dto.request.EventImageUploadUrlRequest;
import com.coma.comaroom.event.dto.response.EventImageUploadUrlResponse;
import com.coma.comaroom.study.storage.OciStorageProperties;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventImageUploadService {

    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(5);
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private final ObjectProvider<ObjectStorage> clients;
    private final OciStorageProperties properties;

    public EventImageUploadUrlResponse createUploadUrl(EventImageUploadUrlRequest request) {
        String extension = IMAGE_EXTENSIONS.get(request.contentType().toLowerCase());
        if (extension == null || request.filename().isBlank()) {
            throw new BusinessException(EventPostError.INVALID_IMAGE);
        }

        properties.validate();
        properties.validateObjectBaseUrl();

        String id = UUID.randomUUID().toString();
        String imageKey = "albums/" + id + "." + extension;
        var details = CreatePreauthenticatedRequestDetails.builder()
                .name("album-upload-" + id)
                .objectName(imageKey)
                .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectWrite)
                .timeExpires(Date.from(Instant.now().plus(UPLOAD_URL_TTL)))
                .build();

        try {
            var response = clients.getObject().createPreauthenticatedRequest(
                    CreatePreauthenticatedRequestRequest.builder()
                            .namespaceName(properties.getNamespace())
                            .bucketName(properties.getBucket())
                            .createPreauthenticatedRequestDetails(details)
                            .build()
            );
            String accessUri = response.getPreauthenticatedRequest().getAccessUri();
            String presignedUrl = "https://objectstorage.%s.oraclecloud.com%s"
                    .formatted(properties.getRegion(), accessUri);
            String objectUrl = stripTrailingSlash(properties.getObjectBaseUrl()) + "/" + imageKey;

            return new EventImageUploadUrlResponse(
                    presignedUrl,
                    imageKey,
                    request.contentType().toLowerCase(),
                    objectUrl
            );
        } catch (RuntimeException exception) {
            throw new BusinessException(EventPostError.IMAGE_UPLOAD_URL_CREATION_FAILED);
        }
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
