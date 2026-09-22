package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.study.StudyError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
public class StudyDownloadSigner {
    private final byte[] secret;

    public StudyDownloadSigner(@Value("${jwt.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String sign(Long studyId, int week, Long materialId, Long memberId, long expires) {
        String payload = "study-material-download:" + studyId + ":" + week + ":" + materialId
                + ":" + memberId + ":" + expires;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("다운로드 URL 서명에 실패했습니다.", ex);
        }
    }

    public void verify(Long studyId, int week, Long materialId, Long memberId, long expires, String signature) {
        if (expires <= Instant.now().getEpochSecond() || signature == null ||
                !MessageDigest.isEqual(sign(studyId, week, materialId, memberId, expires)
                        .getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException(StudyError.ACCESS_DENIED);
        }
    }
}
