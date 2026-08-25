package com.komme.domain.auth.properties;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthPropertiesValidationTests {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    // Bean Validation 테스트 환경 구성
    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    // Bean Validation 리소스 정리 기능
    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    // JWT 설정 필수값 검증
    @Test
    void jwtPropertiesRejectsBlankSecretAndNullDurations() {
        JwtProperties properties = new JwtProperties("", null, null);

        assertThat(propertyNames(validator.validate(properties)))
                .contains("secret", "accessTokenExpiration", "refreshTokenExpiration");
    }

    // 이메일 인증 설정 양수와 필수값 검증
    @Test
    void emailVerificationPropertiesRejectsInvalidValues() {
        EmailVerificationProperties properties = new EmailVerificationProperties(
                null,
                null,
                0,
                null,
                null
        );

        assertThat(propertyNames(validator.validate(properties)))
                .contains(
                        "codeExpiration",
                        "verifiedExpiration",
                        "maxAttempts",
                        "resendCooldown",
                        "lockExpiration"
                );
    }

    // OAuth 설정 필수값 검증
    @Test
    void oAuthPropertiesRejectsBlankClientIdAndNullTtl() {
        AppleProperties appleProperties = new AppleProperties("", null);
        GoogleProperties googleProperties = new GoogleProperties("", "", "", null);

        assertThat(propertyNames(validator.validate(appleProperties)))
                .contains("clientId", "jwksCacheTtl");
        assertThat(propertyNames(validator.validate(googleProperties)))
                .contains("clientId", "clientSecret", "redirectUri", "jwksCacheTtl");
    }

    // 인증 메일 설정 필수값 검증
    @Test
    void authMailPropertiesRejectsBlankSender() {
        AuthMailProperties properties = new AuthMailProperties("");

        assertThat(propertyNames(validator.validate(properties))).contains("sender");
    }

    // 인증 설정 올바른 값 검증 통과 확인
    @Test
    void authPropertiesAcceptValidValues() {
        assertThat(validator.validate(new JwtProperties(
                "test-jwt-secret-key-test-jwt-secret-key-1234567890",
                Duration.ofHours(1),
                Duration.ofDays(14)
        ))).isEmpty();
        assertThat(validator.validate(new EmailVerificationProperties(
                Duration.ofMinutes(5),
                Duration.ofMinutes(30),
                5,
                Duration.ofSeconds(60),
                Duration.ofMinutes(10)
        ))).isEmpty();
        assertThat(validator.validate(new AppleProperties(
                "apple-client-id",
                Duration.ofHours(1)
        ))).isEmpty();
        assertThat(validator.validate(new GoogleProperties(
                "google-client-id",
                "google-client-secret",
                "http://localhost:3000/callback",
                Duration.ofHours(1)
        ))).isEmpty();
        assertThat(validator.validate(new AuthMailProperties("noreply@example.com"))).isEmpty();
    }

    // ConstraintViolation 속성명 집합 생성
    private Set<String> propertyNames(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
