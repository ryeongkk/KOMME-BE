package com.komme.domain.auth.service.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationCodeGeneratorTests {

    private final EmailVerificationCodeGenerator codeGenerator =
            new EmailVerificationCodeGenerator();

    // 6자리 숫자 인증 코드 생성 검증
    @Test
    void generateCreatesSixDigitNumericCode() {
        String verificationCode = codeGenerator.generate();

        assertThat(verificationCode).matches("\\d{6}");
    }
}
