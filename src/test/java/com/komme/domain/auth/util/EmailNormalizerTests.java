package com.komme.domain.auth.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNormalizerTests {

    // 이메일 공백 제거 및 소문자 변환 검증
    @Test
    void normalizeTrimsAndLowercasesEmail() {
        String normalized = EmailNormalizer.normalize(" USER@Example.COM ");

        assertThat(normalized).isEqualTo("user@example.com");
    }
}
