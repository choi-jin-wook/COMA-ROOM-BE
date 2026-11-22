package com.coma.comaroom.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 여러 기능에서 공유하는 storage.oci 설정값과 필수 항목 검사를 정의한다. */
@Getter
@Setter
@ConfigurationProperties(prefix = "storage.oci")
public class OciStorageProperties {
    /** 인스턴스 권한 또는 로컬 설정 파일 인증을 선택한다. */
    public enum Auth { INSTANCE_PRINCIPAL, CONFIG_FILE }
    private String region;
    private String namespace;
    private String bucket;
    private String objectBaseUrl;
    private Auth auth = Auth.INSTANCE_PRINCIPAL;
    private String configFile = "~/.oci/config";
    private String profile = "DEFAULT";

    /** 객체 조회와 업로드에 필요한 리전·네임스페이스·버킷을 확인한다. */
    public void validate() {
        if (region == null || region.isBlank() || namespace == null || namespace.isBlank()
                || bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("storage.oci의 region, namespace, bucket 설정이 필요합니다.");
        }
    }

    /** 외부 객체 URL을 사용할 때 필요한 기본 주소를 확인한다. */
    public void validateObjectBaseUrl() {
        if (objectBaseUrl == null || objectBaseUrl.isBlank()) {
            throw new IllegalStateException("storage.oci.object-base-url 설정이 필요합니다.");
        }
    }
}
