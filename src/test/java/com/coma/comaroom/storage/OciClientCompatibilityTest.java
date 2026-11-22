package com.coma.comaroom.storage;

import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.Base64;
import static org.assertj.core.api.Assertions.assertThat;

/** OCI 클라이언트 구성과 SDK 호환성을 확인한다. */
class OciClientCompatibilityTest {
    @Test
    void sdkHttpClientInitializesWithApplicationDependenciesWithoutNetworkCalls() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(generator.generateKeyPair().getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";
        var auth = SimpleAuthenticationDetailsProvider.builder().tenantId("test-tenant")
                .userId("test-user").fingerprint("00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00")
                .privateKeySupplier(() -> new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))).build();
        try (var client = ObjectStorageClient.builder()
                .endpoint("https://objectstorage.ap-seoul-1.oraclecloud.com").build(auth)) {
            assertThat(client.getEndpoint()).isEqualTo("https://objectstorage.ap-seoul-1.oraclecloud.com");
        }
    }
}
