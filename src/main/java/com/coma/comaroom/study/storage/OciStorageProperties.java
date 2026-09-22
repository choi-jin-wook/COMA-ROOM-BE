package com.coma.comaroom.study.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "study.storage.oci")
public class OciStorageProperties {
    public enum Auth { INSTANCE_PRINCIPAL, CONFIG_FILE }
    private String region;
    private String namespace;
    private String bucket;
    private String objectBaseUrl;
    private Auth auth = Auth.INSTANCE_PRINCIPAL;
    private String configFile = "~/.oci/config";
    private String profile = "DEFAULT";

    public void validate() {
        if (region == null || region.isBlank() || namespace == null || namespace.isBlank()
                || bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("study.storage.oci의 region, namespace, bucket 설정이 필요합니다.");
        }
    }

    public void validateObjectBaseUrl() {
        if (objectBaseUrl == null || objectBaseUrl.isBlank()) {
            throw new IllegalStateException("study.storage.oci.object-base-url 설정이 필요합니다.");
        }
    }
}
