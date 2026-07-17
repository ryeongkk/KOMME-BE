package com.komme.domain.auth.properties;

import java.time.Duration;

import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;

@Getter
@Validated
@ConfigurationProperties(prefix = "auth.email-verification")
public class EmailVerificationProperties {

    @NotNull
    private final Duration codeExpiration;

    // 이메일 인증 설정값 생성
    public EmailVerificationProperties(Duration codeExpiration) {
        this.codeExpiration = codeExpiration;
    }
}
