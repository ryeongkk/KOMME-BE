package com.komme.domain.auth.properties;

import java.time.Duration;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;

@Getter
@Validated
@ConfigurationProperties(prefix = "auth.email-verification")
public class EmailVerificationProperties {

    @NotNull
    private final Duration codeExpiration;

    @NotNull
    private final Duration verifiedExpiration;

    @Positive
    private final int maxAttempts;

    @NotNull
    private final Duration resendCooldown;

    @NotNull
    private final Duration lockExpiration;

    // 이메일 인증 설정값 생성
    public EmailVerificationProperties(
            Duration codeExpiration,
            Duration verifiedExpiration,
            int maxAttempts,
            Duration resendCooldown,
            Duration lockExpiration
    ) {
        this.codeExpiration = codeExpiration;
        this.verifiedExpiration = verifiedExpiration;
        this.maxAttempts = maxAttempts;
        this.resendCooldown = resendCooldown;
        this.lockExpiration = lockExpiration;
    }
}
