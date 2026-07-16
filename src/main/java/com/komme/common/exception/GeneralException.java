package com.komme.common.exception;

import com.komme.common.base.status.BaseStatus;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException{

    private final BaseStatus errorStatus;

    // 에러 상태를 기반으로 일반 예외를 생성
    public GeneralException(
            BaseStatus errorStatus
    ) {
        super(validated(errorStatus).getMessage());
        this.errorStatus = errorStatus;
    }

    // 에러 상태와 원인 예외를 기반으로 일반 예외를 생성
    public GeneralException(
            BaseStatus errorStatus, Throwable cause
    ) {
        super(validated(errorStatus).getMessage(), cause);
        this.errorStatus = errorStatus;
    }

    // 전달된 에러 상태가 null인지 검증
    private static BaseStatus validated(BaseStatus errorStatus) {
        if (errorStatus == null) {
            throw new IllegalArgumentException("errorStatus must not be null");
        }
        return errorStatus;
    }

}
