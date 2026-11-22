package com.coma.comaroom.storage;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import java.io.IOException;

/** 여러 기능에서 공유하는 OCI Object Storage 클라이언트를 지연 생성한다. */
@Configuration
@EnableConfigurationProperties(OciStorageProperties.class)
public class OciStorageConfiguration {
    /** 설정한 인증 방식과 리전으로 클라이언트를 만들고 종료 시 닫는다. */
    @Bean(destroyMethod = "close")
    @Lazy
    public ObjectStorage objectStorageClient(OciStorageProperties properties) throws IOException {
        properties.validate();
        BasicAuthenticationDetailsProvider auth = switch (properties.getAuth()) {
            case INSTANCE_PRINCIPAL -> InstancePrincipalsAuthenticationDetailsProvider.builder().build();
            case CONFIG_FILE -> new ConfigFileAuthenticationDetailsProvider(
                    properties.getConfigFile(), properties.getProfile());
        };
        ObjectStorageClient client = ObjectStorageClient.builder()
                .configuration(ClientConfiguration.builder()
                        .connectionTimeoutMillis(5000).readTimeoutMillis(30000).build())
                .build(auth);
        client.setRegion(properties.getRegion());
        return client;
    }
}
