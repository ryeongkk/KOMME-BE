package com.komme.domain.auth.dto;

import com.komme.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.komme.domain.auth.dto.request.EmailVerificationSendRequest;
import com.komme.domain.auth.dto.request.LoginRequest;
import com.komme.domain.auth.dto.request.LogoutRequest;
import com.komme.domain.auth.dto.request.OAuthAppleLoginRequest;
import com.komme.domain.auth.dto.request.OAuthGoogleLoginRequest;
import com.komme.domain.auth.dto.request.OAuthProfileCompleteRequest;
import com.komme.domain.auth.dto.request.PasswordChangeRequest;
import com.komme.domain.auth.dto.request.PasswordResetConfirmRequest;
import com.komme.domain.auth.dto.request.PasswordResetRequest;
import com.komme.domain.auth.dto.request.PasswordResetSendRequest;
import com.komme.domain.auth.dto.request.SignUpRequest;
import com.komme.domain.auth.dto.request.TokenReissueRequest;

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
                "nickname"
        );

        assertThat(propertyNames(validator.validate(request))).contains("password");
    }

    // 회원가입 요청 특수문자 포함 비밀번호 허용 검증
    @Test
    void signUpRequestAcceptsSpecialCharacterPassword() {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "newpassword2!",
                "nickname"
        );

        assertThat(propertyNames(validator.validate(request))).doesNotContain("password");
    }

    // 형식에 맞지 않는 닉네임 거부 검증
    @Test
    void signUpRequestRejectsInvalidNickname() {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "password1",
                "n"
        );

        assertThat(propertyNames(validator.validate(request))).contains("nickname");
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
        LoginRequest request = new LoginRequest("invalid-email", "", null);

        assertThat(propertyNames(validator.validate(request)))
                .contains("email", "password", "preferredLanguage");
    }

    // Apple 로그인 요청 identity token과 선호 언어 필수값 검증
    @Test
    void oAuthAppleLoginRequestRejectsInvalidValues() {
        OAuthAppleLoginRequest request = new OAuthAppleLoginRequest("", null);

        assertThat(propertyNames(validator.validate(request)))
                .contains("identityToken", "preferredLanguage");
    }

    // Google 로그인 요청 authorization code와 선호 언어 필수값 검증
    @Test
    void oAuthGoogleLoginRequestRejectsInvalidValues() {
        OAuthGoogleLoginRequest request = new OAuthGoogleLoginRequest("", null);

        assertThat(propertyNames(validator.validate(request)))
                .contains("code", "preferredLanguage");
    }

    // OAuth 프로필 완성 요청 올바른 값 검증 통과 확인
    @Test
    void oAuthProfileCompleteRequestAcceptsValidValues() {
        OAuthProfileCompleteRequest request = new OAuthProfileCompleteRequest("nickname");

        assertThat(validator.validate(request)).isEmpty();
    }

    // OAuth 프로필 완성 요청 형식에 맞지 않는 닉네임 거부 검증
    @Test
    void oAuthProfileCompleteRequestRejectsInvalidNickname() {
        OAuthProfileCompleteRequest request = new OAuthProfileCompleteRequest("nick name!");

        assertThat(propertyNames(validator.validate(request))).contains("nickname");
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

    // 비밀번호 변경 요청 특수문자 포함 비밀번호 허용 검증
    @Test
    void passwordChangeRequestAcceptsSpecialCharacterPassword() {
        PasswordChangeRequest request = new PasswordChangeRequest("password1", "newpassword2!");

        assertThat(validator.validate(request)).isEmpty();
    }

    // 비밀번호 재설정 이메일 인증 요청 입력값 검증
    @Test
    void passwordResetVerificationRequestsRejectInvalidValues() {
        PasswordResetSendRequest sendRequest = new PasswordResetSendRequest("invalid-email");
        PasswordResetConfirmRequest confirmRequest = new PasswordResetConfirmRequest(
                "user@example.com",
                "12345a"
        );

        assertThat(propertyNames(validator.validate(sendRequest))).contains("email");
        assertThat(propertyNames(validator.validate(confirmRequest)))
                .contains("verificationCode");
    }

    // 비밀번호 재설정 요청 필수값과 새 비밀번호 형식 검증
    @Test
    void passwordResetRequestRejectsInvalidValues() {
        PasswordResetRequest request = new PasswordResetRequest("", "onlyletters");

        assertThat(propertyNames(validator.validate(request)))
                .contains("resetToken", "newPassword");
    }

    // 비밀번호 재설정 요청 특수문자 포함 비밀번호 허용 검증
    @Test
    void passwordResetRequestAcceptsSpecialCharacterPassword() {
        PasswordResetRequest request = new PasswordResetRequest("reset-token", "newpassword2!");

        assertThat(validator.validate(request)).isEmpty();
    }

    // 정상 회원가입 요청 생성
    private SignUpRequest createValidSignUpRequest() {
        return new SignUpRequest(
                "user@example.com",
                "password1!",
                "nickname"
        );
    }

    // ConstraintViolation 속성명 집합 생성
    private Set<String> propertyNames(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
