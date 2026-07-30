package com.komme.domain.auth.exception;

import com.komme.common.base.status.BaseStatus;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseStatus {

    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "AUTH_400_1", "이메일 인증 코드가 올바르지 않습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "AUTH_400_2", "이메일 인증 코드가 만료되었습니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_400_3", "현재 비밀번호가 올바르지 않습니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_400_4", "지원하지 않는 로그인 방식입니다."),
    APPLE_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_400_5", "Apple 계정의 이메일 정보가 필요합니다."),
    GOOGLE_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_400_6", "Google 계정의 이메일 정보가 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_3", "만료된 토큰입니다."),
    INVALID_APPLE_IDENTITY_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_4", "유효하지 않은 Apple identity token입니다."),
    INVALID_GOOGLE_IDENTITY_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_5", "유효하지 않은 Google identity token입니다."),
    INVALID_RESET_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_6", "유효하지 않은 비밀번호 재설정 토큰입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_403_1", "이메일 인증이 필요합니다."),
    EMAIL_NOT_REGISTERED(HttpStatus.NOT_FOUND, "AUTH_404_1", "가입된 이메일이 아닙니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_409_1", "이미 가입된 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_409_2", "이미 사용 중인 닉네임입니다."),
    OAUTH_ACCOUNT_ALREADY_LINKED(HttpStatus.CONFLICT, "AUTH_409_3", "이미 연결된 OAuth 계정입니다."),
    EMAIL_VERIFICATION_LOCKED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_429_1", "인증 코드 입력 횟수를 초과했습니다."),
    EMAIL_SEND_TOO_FREQUENTLY(HttpStatus.TOO_MANY_REQUESTS, "AUTH_429_2", "잠시 후 인증 이메일을 다시 요청해 주세요."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_500_1", "인증 이메일 전송에 실패했습니다."),
    APPLE_SERVER_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_502_1", "Apple 인증 서버 연결에 실패했습니다."),
    GOOGLE_SERVER_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_502_2", "Google 인증 서버 연결에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
