package com.komme.domain.auth.util;

import java.util.Locale;

public final class EmailNormalizer {

    // 인스턴스 생성 방지
    private EmailNormalizer() {
    }

    // 이메일 앞뒤 공백 제거 및 소문자 변환 기능
    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
