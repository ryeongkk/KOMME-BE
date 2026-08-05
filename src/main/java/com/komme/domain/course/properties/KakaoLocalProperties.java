package com.komme.domain.course.properties;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;

@Getter
@Validated
@ConfigurationProperties(prefix = "kakao")
public class KakaoLocalProperties {

    @NotBlank
    private final String restApiKey;

    // 카카오 로컬 API 설정값 생성
    public KakaoLocalProperties(String restApiKey) {
        this.restApiKey = restApiKey;
    }
}
