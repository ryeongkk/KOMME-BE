package com.komme.domain.auth.properties;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;

@Getter
@Validated
@ConfigurationProperties(prefix = "auth.mail")
public class AuthMailProperties {

    @NotBlank
    private final String sender;

    // 인증 메일 설정값 생성
    public AuthMailProperties(String sender) {
        this.sender = sender;
    }
}
