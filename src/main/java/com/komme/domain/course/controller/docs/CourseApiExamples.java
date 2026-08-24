package com.komme.domain.course.controller.docs;

public final class CourseApiExamples {

    public static final String CREATE_COURSE_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": {
                "courseId": 1,
                "title": null,
                "regionName": "성동구",
                "topics": ["FOOD"],
                "visitDate": "2026-08-10",
                "spots": [
                  {
                    "spotId": 10,
                    "name": "성수동 카페",
                    "sequence": 1,
                    "timeSlot": "MORNING",
                    "latitude": 37.54433,
                    "longitude": 127.05578,
                    "thumbnailUrl": "https://tong.visitkorea.or.kr/thumb.jpg",
                    "distanceToNextMeters": 500
                  }
                ]
              }
            }
            """;

    public static final String DETAIL_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": {
                "courseId": 1,
                "title": "성동구 음식 Day",
                "regionName": "성동구",
                "topics": ["FOOD"],
                "visitDate": "2026-08-10",
                "spots": [
                  {
                    "spotId": 10,
                    "name": "성수동 카페",
                    "sequence": 1,
                    "timeSlot": "MORNING",
                    "latitude": 37.54433,
                    "longitude": 127.05578,
                    "thumbnailUrl": "https://tong.visitkorea.or.kr/thumb.jpg",
                    "distanceToNextMeters": 500
                  }
                ]
              }
            }
            """;

    public static final String SUCCESS_WITHOUT_DATA = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다."
            }
            """;

    public static final String LIST_SUCCESS = """
            {
              "isSuccess": true,
              "code": "COM_200",
              "message": "성공적으로 처리되었습니다.",
              "data": [
                {
                  "courseId": 1,
                  "title": "성동구 음식 Day",
                  "regionName": "성동구",
                  "visitDate": "2026-08-10",
                  "topics": ["FOOD"],
                  "spotCount": 4
                }
              ]
            }
            """;

    public static final String BAD_REQUEST = """
            {
              "isSuccess": false,
              "code": "COM_400",
              "message": "topics: 주제는 최소 1개 이상 선택해야 합니다."
            }
            """;

    public static final String INSUFFICIENT_SPOTS = """
            {
              "isSuccess": false,
              "code": "COURSE_404_1",
              "message": "코스를 구성할 스팟이 부족합니다."
            }
            """;

    public static final String REGION_NOT_FOUND = """
            {
              "isSuccess": false,
              "code": "COURSE_404_3",
              "message": "해당 지역을 찾을 수 없습니다."
            }
            """;

    public static final String REGION_NOT_SUPPORTED = """
            {
              "isSuccess": false,
              "code": "COURSE_400_1",
              "message": "서울/부산 지역만 지원합니다."
            }
            """;

    public static final String COURSE_NOT_FOUND = """
            {
              "isSuccess": false,
              "code": "COURSE_404_2",
              "message": "코스를 찾을 수 없습니다."
            }
            """;

    public static final String INVALID_TOKEN = """
            {
              "isSuccess": false,
              "code": "AUTH_401_2",
              "message": "유효하지 않은 토큰입니다."
            }
            """;

    // 인스턴스 생성 방지
    private CourseApiExamples() {
    }
}
