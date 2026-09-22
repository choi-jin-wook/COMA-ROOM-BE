package com.coma.comaroom.utils;

public final class PhoneNumberNormalizer {

    private static final String KOREA_COUNTRY_CODE = "82";

    private PhoneNumberNormalizer() {
    }

    public static String toKoreanLocalFormat(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return phoneNumber;
        }

        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.startsWith(KOREA_COUNTRY_CODE)) {
            return "0" + digits.substring(KOREA_COUNTRY_CODE.length());
        }

        return digits;
    }
}
