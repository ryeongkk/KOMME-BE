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

    public static final String INVALID_CREDENTIALS = """
            {
              "isSuccess": false,
              "code": "AUTH_401_1",
              "message": "이메일 또는 비밀번호가 올바르지 않습니다."
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

    public static final String NICKNAME_ALREADY_EXISTS = """
            {
              "isSuccess": false,
              "code": "AUTH_409_2",
              "message": "이미 사용 중인 닉네임입니다."
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
