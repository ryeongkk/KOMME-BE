package com.komme.common.base.status;

import lombok.Getter;
import lombok.AllArgsConstructor;
import com.komme.common.base.BaseStatus;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseStatus {

    COMMON_ERROR_STATUS(HttpStatus.BAD_REQUEST, "COM_400","잘못된 요청입니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
