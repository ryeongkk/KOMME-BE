package com.komme.domain.user.controller.docs;

public final class UserApiExamples {

    public static final String SUCCESS_WITHOUT_DATA = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다."
            }
            """;

    public static final String USER_PROFILE_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": {
                "nickname": "komme",
                "provider": "LOCAL",
                "preferredLanguage": "KOREAN"
              }
            }
            """;

    public static final String NICKNAME_AVAILABILITY_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": {
                "available": true
              }
            }
            """;

    public static final String BAD_REQUEST = """
            {
              "isSuccess": false,
              "code": "COM_400",
              "message": "nickname: 닉네임은 필수입니다."
            }
            """;

    public static final String INVALID_TOKEN = """
            {
              "isSuccess": false,
              "code": "AUTH_401_2",
              "message": "유효하지 않은 토큰입니다."
            }
            """;

    public static final String NICKNAME_ALREADY_EXISTS = """
            {
              "isSuccess": false,
              "code": "USER_409_1",
              "message": "이미 사용 중인 닉네임입니다."
            }
            """;

    // 인스턴스 생성 방지
    private UserApiExamples() {
    }
}
