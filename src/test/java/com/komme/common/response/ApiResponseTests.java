package com.komme.common.response;

import com.komme.common.base.status.ErrorStatus;
import com.komme.common.base.status.SuccessStatus;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTests {

    // 데이터 없는 성공 응답 생성 검증
    @Test
    void successWithoutDataCreatesSuccessBody() {
        ResponseEntity<ApiResponse<Void>> response =
                ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS);

        assertThat(response.getStatusCode())
                .isEqualTo(SuccessStatus.COMMON_SUCCESS_STATUS.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsSuccess()).isTrue();
        assertThat(response.getBody().getCode())
                .isEqualTo(SuccessStatus.COMMON_SUCCESS_STATUS.getCode());
        assertThat(response.getBody().getData()).isNull();
    }

    // 데이터 포함 성공 응답 생성 검증
    @Test
    void successWithDataCreatesSuccessBody() {
        ResponseEntity<ApiResponse<String>> response =
                ApiResponse.success(SuccessStatus.COMMON_SUCCESS_STATUS, "data");

        assertThat(response.getStatusCode())
                .isEqualTo(SuccessStatus.COMMON_SUCCESS_STATUS.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo("data");
    }

    // 기본 실패 응답 생성 검증
    @Test
    void errorCreatesErrorBody() {
        ResponseEntity<ApiResponse<Void>> response =
                ApiResponse.error(ErrorStatus.BAD_REQUEST);

        assertThat(response.getStatusCode()).isEqualTo(ErrorStatus.BAD_REQUEST.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorStatus.BAD_REQUEST.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorStatus.BAD_REQUEST.getMessage());
    }

    // 커스텀 메시지 실패 응답 생성 검증
    @Test
    void errorWithCustomMessageCreatesErrorBody() {
        ResponseEntity<ApiResponse<Void>> response =
                ApiResponse.error(ErrorStatus.BAD_REQUEST, "custom message");

        assertThat(response.getStatusCode()).isEqualTo(ErrorStatus.BAD_REQUEST.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("custom message");
    }
}
