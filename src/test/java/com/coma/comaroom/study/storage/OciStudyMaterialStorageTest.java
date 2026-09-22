package com.coma.comaroom.study.storage;

import com.coma.comaroom.study.StudyError;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import java.io.ByteArrayInputStream;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OciStudyMaterialStorageTest {
    private final ObjectStorage client = mock(ObjectStorage.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<ObjectStorage> provider = mock(ObjectProvider.class);
    private final OciStorageProperties properties = new OciStorageProperties();
    private final OciStudyMaterialStorage storage = new OciStudyMaterialStorage(provider, properties);

    OciStudyMaterialStorageTest() {
        properties.setRegion("ap-seoul-1");
        properties.setNamespace("test-namespace");
        properties.setBucket("test-bucket");
        when(provider.getObject()).thenReturn(client);
    }

    @Test
    void uploadUsesConfiguredBucketAndObjectKey() throws Exception {
        storage.upload("studies/1/weeks/2/key", "text/plain", "notes".getBytes());
        var captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(client).putObject(captor.capture());
        var request = captor.getValue();
        assertThat(request.getNamespaceName()).isEqualTo("test-namespace");
        assertThat(request.getBucketName()).isEqualTo("test-bucket");
        assertThat(request.getObjectName()).isEqualTo("studies/1/weeks/2/key");
        assertThat(request.getContentLength()).isEqualTo(5);
        assertThat(request.getContentType()).isEqualTo("text/plain");
        assertThat(request.getPutObjectBody().readAllBytes()).isEqualTo("notes".getBytes());
    }

    @Test
    void downloadsAndClosesOciStream() throws Exception {
        var stream = spy(new ByteArrayInputStream("notes".getBytes()));
        when(client.getObject(any())).thenReturn(GetObjectResponse.builder().inputStream(stream).build());
        assertThat(storage.download("key", 5)).isEqualTo("notes".getBytes());
        verify(stream).close();
    }

    @Test
    void deletedObjectsAreNotFoundAndOutagesAreNotEmptySuccesses() {
        BmcException missing = mock(BmcException.class);
        when(missing.getStatusCode()).thenReturn(404);
        when(client.headObject(any())).thenThrow(missing);
        assertThatThrownBy(() -> storage.checkExists("key")).hasMessage(StudyError.MATERIAL_NOT_FOUND.getMessage());
        when(client.getObject(any())).thenThrow(new IllegalStateException("unavailable"));
        assertThatThrownBy(() -> storage.download("key", 5)).hasMessage(StudyError.STORAGE_UNAVAILABLE.getMessage());
    }

    @Test
    void oversizedOrTruncatedObjectsAreRejected() {
        when(client.getObject(any())).thenReturn(GetObjectResponse.builder()
                .inputStream(new ByteArrayInputStream("too-long".getBytes())).build());
        assertThatThrownBy(() -> storage.download("key", 5)).hasMessage(StudyError.STORAGE_UNAVAILABLE.getMessage());
        when(client.getObject(any())).thenReturn(GetObjectResponse.builder()
                .inputStream(new ByteArrayInputStream(new byte[0])).build());
        assertThatThrownBy(() -> storage.download("key", 5)).hasMessage(StudyError.STORAGE_UNAVAILABLE.getMessage());
    }

    @Test
    void missingConfigurationFailsWithoutAccessingCredentials() {
        properties.setBucket(null);
        assertThatThrownBy(() -> storage.checkExists("key")).hasMessage(StudyError.STORAGE_UNAVAILABLE.getMessage());
        verifyNoInteractions(client);
    }

    @Test
    void cleanupDoesNotHideOriginalTransactionFailure() {
        when(client.deleteObject(any())).thenThrow(new IllegalStateException("unavailable"));
        assertThatNoException().isThrownBy(() -> storage.deleteAfterRollback("key"));
        verify(client).deleteObject(any());
    }
}
