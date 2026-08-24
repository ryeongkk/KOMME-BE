package com.komme.domain.auth.controller.docs;

public final class OAuthApiExamples {

    public static final String APPLE_LOGIN_REQUEST = """
            {
              "identityToken": "eyJraWQiOiJ...",
              "preferredLanguage": "ENGLISH"
            }
            """;

    public static final String APPLE_LOGIN_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                "profileCompleted": true
              }
            }
            """;

    public static final String APPLE_EMAIL_REQUIRED = """
            {
              "isSuccess": false,
              "code": "AUTH_400_5",
              "message": "Apple 계정의 이메일 정보가 필요합니다."
            }
            """;

    public static final String INVALID_APPLE_IDENTITY_TOKEN = """
            {
              "isSuccess": false,
              "code": "AUTH_401_4",
              "message": "유효하지 않은 Apple identity token입니다."
            }
            """;

    public static final String OAUTH_ACCOUNT_ALREADY_LINKED = """
            {
              "isSuccess": false,
              "code": "AUTH_409_3",
              "message": "이미 연결된 OAuth 계정입니다."
            }
            """;

    public static final String APPLE_SERVER_CONNECTION_FAILED = """
            {
              "isSuccess": false,
              "code": "AUTH_502_1",
              "message": "Apple 인증 서버 연결에 실패했습니다."
            }
            """;

    public static final String GOOGLE_LOGIN_REQUEST = """
            {
              "idToken": "eyJhbGciOiJSUzI1NiIs...",
              "preferredLanguage": "ENGLISH"
            }
            """;

    public static final String GOOGLE_LOGIN_SUCCESS = APPLE_LOGIN_SUCCESS;

    public static final String GOOGLE_EMAIL_REQUIRED = """
            {
              "isSuccess": false,
              "code": "AUTH_400_6",
              "message": "Google 계정의 이메일 정보가 필요합니다."
            }
            """;

    public static final String INVALID_GOOGLE_IDENTITY_TOKEN = """
            {
              "isSuccess": false,
              "code": "AUTH_401_5",
              "message": "유효하지 않은 Google identity token입니다."
            }
            """;

    public static final String GOOGLE_SERVER_CONNECTION_FAILED = """
            {
              "isSuccess": false,
              "code": "AUTH_502_2",
              "message": "Google 인증 서버 연결에 실패했습니다."
            }
            """;

    // 인스턴스 생성 방지
    private OAuthApiExamples() {
    }
}
