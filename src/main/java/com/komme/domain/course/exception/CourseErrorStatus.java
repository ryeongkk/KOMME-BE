package com.komme.domain.course.exception;

import com.komme.common.base.status.BaseStatus;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseErrorStatus implements BaseStatus {

    KAKAO_LOCAL_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "COURSE_502_1", "카카오 로컬 API 응답이 올바르지 않습니다."),
    KAKAO_LOCAL_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "COURSE_502_2", "카카오 로컬 API 연결에 실패했습니다."),
    INSUFFICIENT_SPOTS(HttpStatus.NOT_FOUND, "COURSE_404_1", "코스를 구성할 스팟이 부족합니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_404_2", "코스를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
