package com.komme.domain.auth.controller.docs;

public final class AuthApiExamples {

    public static final String SUCCESS_WITHOUT_DATA = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다."
            }
            """;

    public static final String LOGIN_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
              }
            }
            """;

    public static final String TOKEN_REISSUE_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
              }
            }
            """;

    public static final String PASSWORD_RESET_TOKEN_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": {
                "resetToken": "w5ME7pKqBlj8xj-DaYcpCV8RpYa70PsTv_oak93wxqs"
              }
            }
            """;

    public static final String BAD_REQUEST = """
            {
              "isSuccess": false,
              "code": "COM_400",
              "message": "email: 올바른 이메일 형식이 아닙니다."
            }
            """;

    public static final String INVALID_VERIFICATION_CODE = """
            {
              "isSuccess": false,
              "code": "AUTH_400_1",
              "message": "이메일 인증 코드가 올바르지 않습니다."
            }
            """;

    public static final String EXPIRED_VERIFICATION_CODE = """
            {
              "isSuccess": false,
              "code": "AUTH_400_2",
              "message": "이메일 인증 코드가 만료되었습니다."
            }
            """;

    public static final String INVALID_CURRENT_PASSWORD = """
            {
              "isSuccess": false,
              "code": "AUTH_400_3",
              "message": "현재 비밀번호가 올바르지 않습니다."
            }
            """;

    public static final String INVALID_CREDENTIALS = """
            {
              "isSuccess": false,
              "code": "AUTH_401_1",
              "message": "이메일 또는 비밀번호가 올바르지 않습니다."
            }
            """;

    public static final String INVALID_TOKEN = """
            {
              "isSuccess": false,
              "code": "AUTH_401_2",
              "message": "유효하지 않은 토큰입니다."
            }
            """;

    public static final String EXPIRED_TOKEN = """
            {
              "isSuccess": false,
              "code": "AUTH_401_3",
              "message": "만료된 토큰입니다."
            }
            """;

    public static final String INVALID_RESET_TOKEN = """
            {
              "isSuccess": false,
              "code": "AUTH_401_6",
              "message": "유효하지 않은 비밀번호 재설정 토큰입니다."
            }
            """;

    public static final String EMAIL_NOT_VERIFIED = """
            {
              "isSuccess": false,
              "code": "AUTH_403_1",
              "message": "이메일 인증이 필요합니다."
            }
            """;

    public static final String EMAIL_ALREADY_EXISTS = """
            {
              "isSuccess": false,
              "code": "AUTH_409_1",
              "message": "이미 가입된 이메일입니다."
            }
            """;

    public static final String EMAIL_NOT_REGISTERED = """
            {
              "isSuccess": false,
              "code": "AUTH_404_1",
              "message": "가입된 이메일이 아닙니다."
            }
            """;

    public static final String NICKNAME_ALREADY_EXISTS = """
            {
              "isSuccess": false,
              "code": "AUTH_409_2",
              "message": "이미 사용 중인 닉네임입니다."
            }
            """;

    public static final String WITHDRAWAL_GRACE_PERIOD = """
            {
              "isSuccess": false,
              "code": "AUTH_409_4",
              "message": "탈퇴 후 7일간 재가입할 수 없습니다."
            }
            """;

    public static final String EMAIL_VERIFICATION_LOCKED = """
            {
              "isSuccess": false,
              "code": "AUTH_429_1",
              "message": "인증 코드 입력 횟수를 초과했습니다."
            }
            """;

    public static final String EMAIL_SEND_TOO_FREQUENTLY = """
            {
              "isSuccess": false,
              "code": "AUTH_429_2",
              "message": "잠시 후 인증 이메일을 다시 요청해 주세요."
            }
            """;

    public static final String EMAIL_SEND_FAILED = """
            {
              "isSuccess": false,
              "code": "AUTH_500_1",
              "message": "인증 이메일 전송에 실패했습니다."
            }
            """;

    // 인스턴스 생성 방지
    private AuthApiExamples() {
    }
}
