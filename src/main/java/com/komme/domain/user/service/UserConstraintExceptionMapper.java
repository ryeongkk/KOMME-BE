package com.komme.domain.user.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.user.exception.UserErrorStatus;

import org.hibernate.exception.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class UserConstraintExceptionMapper {

    // 사용자 데이터 제약조건 예외 변환 기능
    public RuntimeException map(
            DataIntegrityViolationException exception,
            UserErrorStatus fallbackStatus
    ) {
        UserErrorStatus errorStatus = resolveErrorStatus(
                findConstraintName(exception),
                fallbackStatus
        );

        if (errorStatus == null) {
            return exception;
        }

        return new GeneralException(errorStatus, exception);
    }

    // 제약조건명 기반 사용자 오류 상태 조회 기능
    private UserErrorStatus resolveErrorStatus(
            String constraintName,
            UserErrorStatus fallbackStatus
    ) {
        if ("uk_user_nickname".equalsIgnoreCase(constraintName)) {
            return UserErrorStatus.NICKNAME_ALREADY_EXISTS;
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
