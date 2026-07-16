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
        super(errorStatus.getMessage());
        validateErrorStatus(errorStatus);
        this.errorStatus = errorStatus;
    }

    // 에러 상태와 원인 예외를 기반으로 일반 예외를 생성
    public GeneralException(
            BaseStatus errorStatus, Throwable cause
    ) {
        super(errorStatus.getMessage(), cause);
        validateErrorStatus(errorStatus);
        this.errorStatus = errorStatus;
    }

    // 전달된 에러 상태가 null인지 검증
    private static void validateErrorStatus(
            BaseStatus errorStatus
    ){
        if(errorStatus == null){
            throw new IllegalArgumentException("errorStatus must not be null");
        }
    }

}
