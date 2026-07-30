package com.komme.domain.auth.service.email;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class EmailVerificationCodeGenerator {

    private static final int VERIFICATION_CODE_BOUND = 1_000_000;

    private final SecureRandom secureRandom = new SecureRandom();

    // 보안 난수 기반 6자리 인증 코드 생성
    public String generate() {
        return "%06d".formatted(secureRandom.nextInt(VERIFICATION_CODE_BOUND));
    }
}
