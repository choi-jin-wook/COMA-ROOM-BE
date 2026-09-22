package com.coma.comaroom.study.storage;

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

@Configuration
@EnableConfigurationProperties(OciStorageProperties.class)
public class OciStorageConfiguration {
    @Bean(destroyMethod = "close")
    @Lazy
    public ObjectStorage studyObjectStorageClient(OciStorageProperties properties) throws IOException {
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
