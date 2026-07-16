package com.komme.common.base.status;

import lombok.Getter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseStatus {

    COMMON_SUCCESS_STATUS(HttpStatus.OK, "COM_200", "성공적으로 처리되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
