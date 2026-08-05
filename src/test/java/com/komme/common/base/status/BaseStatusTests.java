package com.komme.common.base.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class BaseStatusTests {

    // 공통 에러 상태 BaseStatus 계약 검증
    @Test
    void errorStatusImplementsBaseStatusContract() {
        BaseStatus status = ErrorStatus.UNAUTHORIZED;

        assertThat(status.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(status.getCode()).isEqualTo("COM_401");
        assertThat(status.getMessage()).isEqualTo("인증이 필요합니다.");
    }

    // 공통 성공 상태 BaseStatus 계약 검증
    @Test
    void successStatusImplementsBaseStatusContract() {
        BaseStatus status = SuccessStatus.COMMON_SUCCESS_STATUS;

        assertThat(status.getHttpStatus()).isEqualTo(HttpStatus.OK);
        assertThat(status.getCode()).isEqualTo("COM_200");
        assertThat(status.getMessage()).isEqualTo("성공적으로 처리되었습니다.");
    }

    // 공통 에러 상태 HTTP 코드 순서 검증
    @Test
    void errorStatusEntriesAreOrderedByHttpStatus() {
        assertThat(ErrorStatus.values())
                .extracting(status -> status.getHttpStatus().value())
                .containsExactly(400, 401, 403, 404, 405, 409, 415, 422, 429, 500);
    }
}
