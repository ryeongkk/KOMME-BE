package com.komme.domain.auth.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordPolicyTests {

    // 비밀번호 정책 유효값 허용 검증
    @Test
    void patternAcceptsValidPassword() {
        assertThat("abc12345").matches(PasswordPolicy.PATTERN);
        assertThat("Abc12345!").matches(PasswordPolicy.PATTERN);
    }

    // 비밀번호 정책 영문 누락 거부 검증
    @Test
    void patternRejectsPasswordWithoutLetter() {
        assertThat("12345678").doesNotMatch(PasswordPolicy.PATTERN);
    }

    // 비밀번호 정책 숫자 누락 거부 검증
    @Test
    void patternRejectsPasswordWithoutDigit() {
        assertThat("abcdefgh").doesNotMatch(PasswordPolicy.PATTERN);
    }

    // 비밀번호 정책 길이 범위 거부 검증
    @Test
    void patternRejectsInvalidLength() {
        assertThat("a123456").doesNotMatch(PasswordPolicy.PATTERN);
        assertThat("a12345678901234567890").doesNotMatch(PasswordPolicy.PATTERN);
    }
}
