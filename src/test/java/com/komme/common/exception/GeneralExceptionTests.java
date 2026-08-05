package com.komme.common.exception;

import com.komme.common.base.status.ErrorStatus;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneralExceptionTests {

    // 에러 상태 기반 예외 생성 검증
    @Test
    void constructorStoresErrorStatusAndMessage() {
        GeneralException exception = new GeneralException(ErrorStatus.BAD_REQUEST);

        assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo(ErrorStatus.BAD_REQUEST.getMessage());
    }

    // 에러 상태와 원인 예외 기반 예외 생성 검증
    @Test
    void constructorStoresCause() {
        RuntimeException cause = new RuntimeException("cause");

        GeneralException exception = new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR, cause);

        assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getCause()).isSameAs(cause);
    }

    // null 에러 상태 예외 생성 거부 검증
    @Test
    void constructorRejectsNullErrorStatus() {
        assertThatThrownBy(() -> new GeneralException(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("errorStatus must not be null");
    }
}
