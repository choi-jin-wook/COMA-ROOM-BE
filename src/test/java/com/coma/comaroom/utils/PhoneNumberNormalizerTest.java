package com.coma.comaroom.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberNormalizerTest {

    @Test
    void convertsKakaoPhoneNumberToKoreanLocalFormat() {
        assertThat(PhoneNumberNormalizer.toKoreanLocalFormat("+82 10 6565 4451"))
                .isEqualTo("01065654451");
    }

    @Test
    void removesSeparatorsFromLocalPhoneNumber() {
        assertThat(PhoneNumberNormalizer.toKoreanLocalFormat("010-6565-4451"))
                .isEqualTo("01065654451");
    }

    @Test
    void preservesNullAndBlankValues() {
        assertThat(PhoneNumberNormalizer.toKoreanLocalFormat(null)).isNull();
        assertThat(PhoneNumberNormalizer.toKoreanLocalFormat(" ")).isEqualTo(" ");
    }
}
