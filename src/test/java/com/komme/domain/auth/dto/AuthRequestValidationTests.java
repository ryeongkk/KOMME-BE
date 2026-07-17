package com.komme.domain.auth.dto;

import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.LogoutRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;
import com.komme.domain.auth.dto.request.TokenReissueRequest;
import com.komme.domain.auth.enums.Gender;
import com.komme.domain.auth.enums.ServiceInterest;
import com.komme.i18n.enums.Language;

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

class AuthRequestValidationTests {

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

    // 올바른 회원가입 요청 검증 통과 확인
    @Test
    void signUpRequestAcceptsValidValues() {
        assertThat(validator.validate(createValidSignUpRequest())).isEmpty();
    }

    // 영문과 숫자 조합이 아닌 비밀번호 거부 검증
    @Test
    void signUpRequestRejectsInvalidPassword() {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "password",
                "nickname",
                "KR",
                Gender.FEMALE,
                Language.ENGLISH,
                Set.of(ServiceInterest.COURSE)
        );

        assertThat(propertyNames(validator.validate(request))).contains("password");
    }

    // 잘못된 국적과 필수 선택값 거부 검증
    @Test
    void signUpRequestRejectsInvalidProfileValues() {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "password1",
                "nickname",
                "KOR",
                null,
                null,
                Set.of()
        );

        assertThat(propertyNames(validator.validate(request)))
                .contains("nationality", "gender", "preferredLanguage", "serviceInterests");
    }

    // 이메일 인증 전송 요청 이메일 형식 검증
    @Test
    void emailVerificationSendRequestRejectsInvalidEmail() {
        EmailVerificationSendRequest request = new EmailVerificationSendRequest("invalid-email");

        assertThat(propertyNames(validator.validate(request))).contains("email");
    }

    // 이메일 인증 확인 요청 코드 형식 검증
    @Test
    void emailVerificationConfirmRequestRejectsInvalidCode() {
        EmailVerificationConfirmRequest request = new EmailVerificationConfirmRequest(
                "user@example.com",
                "12345a"
        );

        assertThat(propertyNames(validator.validate(request))).contains("verificationCode");
    }

    // 로그인 요청 필수값 검증
    @Test
    void loginRequestRejectsInvalidValues() {
        LoginRequest request = new LoginRequest("invalid-email", "");

        assertThat(propertyNames(validator.validate(request))).contains("email", "password");
    }

    // 토큰 요청 필수값 검증
    @Test
    void tokenRequestsRejectBlankRefreshToken() {
        assertThat(propertyNames(validator.validate(new TokenReissueRequest(""))))
                .contains("refreshToken");
        assertThat(propertyNames(validator.validate(new LogoutRequest(""))))
                .contains("refreshToken");
    }

    // 비밀번호 변경 요청 새 비밀번호 형식 검증
    @Test
    void passwordChangeRequestRejectsInvalidNewPassword() {
        PasswordChangeRequest request = new PasswordChangeRequest("password1", "onlyletters");

        assertThat(propertyNames(validator.validate(request))).contains("newPassword");
    }

    // 정상 회원가입 요청 생성
    private SignUpRequest createValidSignUpRequest() {
        return new SignUpRequest(
                "user@example.com",
                "password1",
                "nickname",
                "KR",
                Gender.FEMALE,
                Language.ENGLISH,
                Set.of(ServiceInterest.COURSE)
        );
    }

    // ConstraintViolation 속성명 집합 생성
    private Set<String> propertyNames(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
