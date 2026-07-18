package com.komme.domain.user.service;

import com.komme.common.exception.GeneralException;
import com.komme.domain.user.exception.UserConstraintExceptionMapper;
import com.komme.domain.user.exception.UserErrorStatus;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserConstraintExceptionMapperTests {

    private final UserConstraintExceptionMapper mapper = new UserConstraintExceptionMapper();

    // 닉네임 unique 제약조건 예외 변환 검증
    @Test
    void mapConvertsNicknameUniqueConstraintViolation() {
        DataIntegrityViolationException exception = createUniqueConstraintException(
                "uk_user_nickname"
        );

        assertThatThrownBy(() -> {
            throw mapper.map(exception, null);
        })
                .isInstanceOf(GeneralException.class)
                .extracting(error -> ((GeneralException) error).getErrorStatus())
                .isEqualTo(UserErrorStatus.NICKNAME_ALREADY_EXISTS);
    }

    // 알 수 없는 제약조건 예외 유지 검증
    @Test
    void mapReturnsOriginalExceptionWhenUnknownConstraintAndNoFallback() {
        DataIntegrityViolationException exception = createUniqueConstraintException(
                "unknown_constraint"
        );

        RuntimeException result = mapper.map(exception, null);

        assertThat(result).isSameAs(exception);
    }

    // Hibernate unique 제약조건 예외 생성
    private DataIntegrityViolationException createUniqueConstraintException(
            String constraintName
    ) {
        ConstraintViolationException cause = mock(ConstraintViolationException.class);
        when(cause.getConstraintName()).thenReturn(constraintName);
        return new DataIntegrityViolationException("unique constraint", cause);
    }
}
