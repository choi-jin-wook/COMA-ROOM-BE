package com.coma.comaroom.study.service;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

/** 자료 다운로드 서명의 생성·검증 조건을 확인한다. */
class StudyDownloadSignerTest {
    private final StudyDownloadSigner signer = new StudyDownloadSigner("test-signing-secret");

    @Test
    void rejectsExpiredTamperedAndOtherMemberLinks() {
        long expires = Instant.now().plusSeconds(300).getEpochSecond();
        String signature = signer.sign(1L, 2, 3L, 4L, expires);
        assertThatNoException().isThrownBy(() -> signer.verify(1L, 2, 3L, 4L, expires, signature));
        assertThatThrownBy(() -> signer.verify(1L, 2, 3L, 5L, expires, signature)).isNotNull();
        assertThatThrownBy(() -> signer.verify(1L, 3, 3L, 4L, expires, signature)).isNotNull();
        assertThatThrownBy(() -> signer.verify(1L, 2, 9L, 4L, expires, signature)).isNotNull();
        assertThatThrownBy(() -> signer.verify(1L, 2, 3L, 4L, expires + 1, signature)).isNotNull();
        long expired = Instant.now().minusSeconds(1).getEpochSecond();
        assertThatThrownBy(() -> signer.verify(1L, 2, 3L, 4L, expired,
                signer.sign(1L, 2, 3L, 4L, expired))).isNotNull();
    }
}
