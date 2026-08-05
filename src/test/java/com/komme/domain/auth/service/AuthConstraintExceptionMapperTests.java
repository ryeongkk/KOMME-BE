package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;

class AuthConstraintExceptionMapperTests {

    private final AuthConstraintExceptionMapper mapper = new AuthConstraintExceptionMapper();

    // 이메일 유니크 제약조건 인증 오류 변환 검증
    @Test
    void mapConvertsEmailConstraint() {
        RuntimeException mapped = mapper.map(createException("uk_user_email"), null);

        assertThat(mapped)
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
    }

    // 닉네임 유니크 제약조건 인증 오류 변환 검증
    @Test
    void mapConvertsNicknameConstraint() {
        RuntimeException mapped = mapper.map(createException("uk_user_nickname"), null);

        assertThat(mapped)
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.NICKNAME_ALREADY_EXISTS);
    }

    // OAuth 계정 유니크 제약조건 인증 오류 변환 검증
    @Test
    void mapConvertsOAuthAccountConstraint() {
        RuntimeException mapped = mapper.map(createException("uk_oauth_account_provider_id"), null);

        assertThat(mapped)
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.OAUTH_ACCOUNT_ALREADY_LINKED);
    }

    // 미확인 제약조건 fallback 오류 변환 검증
    @Test
    void mapUsesFallbackStatusForUnknownConstraint() {
        RuntimeException mapped = mapper.map(
                createException("unknown_constraint"),
                AuthErrorStatus.EMAIL_ALREADY_EXISTS
        );

        assertThat(mapped)
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorStatus())
                .isEqualTo(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
    }

    // fallback 없는 미확인 제약조건 원본 반환 검증
    @Test
    void mapReturnsOriginalExceptionWithoutResolvedStatus() {
        DataIntegrityViolationException exception = createException("unknown_constraint");

        RuntimeException mapped = mapper.map(exception, null);

        assertThat(mapped).isSameAs(exception);
    }

    // 제약조건 위반 예외 생성
    private DataIntegrityViolationException createException(String constraintName) {
        return new DataIntegrityViolationException(
                "duplicate",
                new ConstraintViolationException("duplicate", null, constraintName)
        );
    }
}
