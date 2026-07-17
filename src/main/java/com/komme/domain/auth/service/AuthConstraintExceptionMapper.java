package com.komme.domain.auth.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.auth.exception.AuthErrorStatus;

import org.hibernate.exception.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class AuthConstraintExceptionMapper {

    // 인증 데이터 제약조건 예외 변환 기능
    public RuntimeException map(
            DataIntegrityViolationException exception,
            AuthErrorStatus fallbackStatus
    ) {
        AuthErrorStatus errorStatus = resolveErrorStatus(
                findConstraintName(exception),
                fallbackStatus
        );

        if (errorStatus == null) {
            return exception;
        }

        return new GeneralException(errorStatus, exception);
    }

    // 제약조건명 기반 인증 오류 상태 조회 기능
    private AuthErrorStatus resolveErrorStatus(
            String constraintName,
            AuthErrorStatus fallbackStatus
    ) {
        if ("uk_user_email".equalsIgnoreCase(constraintName)) {
            return AuthErrorStatus.EMAIL_ALREADY_EXISTS;
        }

        if ("uk_user_nickname".equalsIgnoreCase(constraintName)) {
            return AuthErrorStatus.NICKNAME_ALREADY_EXISTS;
        }

        if ("uk_oauth_account_provider_id".equalsIgnoreCase(constraintName)) {
            return AuthErrorStatus.OAUTH_ACCOUNT_ALREADY_LINKED;
        }

        return fallbackStatus;
    }

    // 예외 원인 체인의 Hibernate 제약조건명 조회 기능
    private String findConstraintName(Throwable throwable) {
        Throwable cause = throwable;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                return constraintViolationException.getConstraintName();
            }
            cause = cause.getCause();
        }

        return null;
    }
}
