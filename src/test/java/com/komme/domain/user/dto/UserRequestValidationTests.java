package com.komme.domain.user.dto;

import com.komme.domain.i18n.enums.Language;
import com.komme.domain.user.dto.request.ChangeNicknameRequest;
import com.komme.domain.user.dto.request.ChangePreferredLanguageRequest;
import com.komme.domain.user.dto.request.UpdateTermsAgreementRequest;

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

class UserRequestValidationTests {

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

    // 닉네임 변경 요청 올바른 값 검증 통과 확인
    @Test
    void changeNicknameRequestAcceptsValidValue() {
        ChangeNicknameRequest request = new ChangeNicknameRequest("nickname2");

        assertThat(validator.validate(request)).isEmpty();
    }

    // 닉네임 변경 요청 형식에 맞지 않는 닉네임 거부 검증
    @Test
    void changeNicknameRequestRejectsInvalidNickname() {
        ChangeNicknameRequest request = new ChangeNicknameRequest("n");

        assertThat(propertyNames(validator.validate(request))).contains("nickname");
    }

    // 선호 언어 변경 요청 올바른 값 검증 통과 확인
    @Test
    void changePreferredLanguageRequestAcceptsValidValue() {
        ChangePreferredLanguageRequest request =
                new ChangePreferredLanguageRequest(Language.ENGLISH);

        assertThat(validator.validate(request)).isEmpty();
    }

    // 선호 언어 변경 요청 필수값 거부 검증
    @Test
    void changePreferredLanguageRequestRejectsNullValue() {
        ChangePreferredLanguageRequest request = new ChangePreferredLanguageRequest(null);

        assertThat(propertyNames(validator.validate(request))).contains("preferredLanguage");
    }

    // 선택 약관 변경 요청 올바른 값 검증 통과 확인
    @Test
    void updateTermsAgreementRequestAcceptsValidValue() {
        UpdateTermsAgreementRequest request = new UpdateTermsAgreementRequest(false);

        assertThat(validator.validate(request)).isEmpty();
    }

    // 선택 약관 변경 요청 필수값 거부 검증
    @Test
    void updateTermsAgreementRequestRejectsNullValue() {
        UpdateTermsAgreementRequest request = new UpdateTermsAgreementRequest(null);

        assertThat(propertyNames(validator.validate(request))).contains("agreed");
    }

    // ConstraintViolation 속성명 집합 생성
    private Set<String> propertyNames(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
