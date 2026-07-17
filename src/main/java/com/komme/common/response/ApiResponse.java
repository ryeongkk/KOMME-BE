package com.komme.common.response;

import com.komme.common.base.status.BaseStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "data"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private Boolean isSuccess;
    private String code;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // 데이터 없이 성공 응답 생성
    public static ResponseEntity<ApiResponse<Void>> success(
            BaseStatus successStatus
    ) {
        return ResponseEntity
                .status(successStatus.getHttpStatus())
                .body(new ApiResponse<>(true, successStatus.getCode(), successStatus.getMessage(), null));
    }

    // 데이터를 포함한 성공 응답 생성
    public static <T> ResponseEntity<ApiResponse<T>> success(
            BaseStatus successStatus, T data
    ) {
        return ResponseEntity
                .status(successStatus.getHttpStatus())
                .body(new ApiResponse<>(true, successStatus.getCode(), successStatus.getMessage(), data));
    }

    // 데이터 없이 실패 응답 생성
    public static ResponseEntity<ApiResponse<Void>> error(
            BaseStatus errorStatus
    ) {
        return ResponseEntity
                .status(errorStatus.getHttpStatus())
                .body(new ApiResponse<>(false, errorStatus.getCode(), errorStatus.getMessage(), null));
    }

    // 커스텀 메시지로 실패 응답 생성
    public static ResponseEntity<ApiResponse<Void>> error(
            BaseStatus errorStatus,
            String message
    ){
        return ResponseEntity
                .status(errorStatus.getHttpStatus())
                .body(new ApiResponse<>(false, errorStatus.getCode(), message, null));
    }

}
