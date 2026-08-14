---
paths:
  - "src/main/java/com/komme/domain/course/**"
---

# course 도메인 규칙

KOMME의 '맞춤형 데일리 코스 생성'과 '즉시 체험 추천' 기능이 속한 패키지다. 상세 배경/의존 관계는 [[course-agent]] 참고.

- 코스는 시간대별 흐름(아침/점심/저녁)을 반영해 구성한다. 스팟을 개수만 채워 반환하는 방식으로 구현하지 않는다.
- 스팟 원본 데이터를 이 패키지에서 관광공사 API로 직접 호출하지 않는다 — `com.komme.tourapi` 클라이언트를 통해서만 가져온다.
- 컨트롤러는 `ApiResponse.success/error` 정적 팩토리로만 응답을 만든다.
- 도메인 실패는 `GeneralException` + 전용 `BaseStatus` enum(예: `CourseErrorStatus`)으로 표현하고, `ErrorStatus`를 무한정 늘리지 않는다.
- 엔티티는 `BaseEntity`를 상속한다.
- "생성"과 "저장"은 다른 개념이다. `Course`는 생성 시점에 무조건 DB에 저장되지만 제목이 없고, `Course.user`는 "생성자"일 뿐 저장 전 코스에 대한 소유권 체크(존재 숨김용 404)에만 쓰인다. 실제 "내 코스 목록"에 뜨려면 `UserCourse`((user, course) 유니크, 사용자가 저장 시 입력한 `title` 보유)가 별도로 생성돼야 한다 — `CourseQueryService.findList`는 `Course`가 아니라 `UserCourse` 기준으로 조회한다. 코스 삭제 시 `CourseSpot`과 `UserCourse`를 `Course`보다 먼저 지운다.
