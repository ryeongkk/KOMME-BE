---
paths:
  - "src/main/java/com/komme/spot/**"
---

# spot 도메인 규칙

KOMME에서 방문 가능한 개별 장소(스팟)를 표현하는 패키지다. 상세 배경/의존 관계는 [[spot-agent]] 참고.

- 관광공사 API 원본 응답 DTO를 컨트롤러 응답이나 다른 도메인에 그대로 노출하지 않는다. 반드시 내부 스팟 모델로 매핑해서 제공한다.
- 원본 데이터를 이 패키지에서 관광공사 API로 직접 호출하지 않는다 — `com.komme.tourapi` 클라이언트를 통해서만 가져온다.
- 위치 기반 조회는 기본 반경 3km, 거리순 정렬을 기본값으로 한다 (제안서 MVP 기준). 값 변경이 필요하면 사용자와 먼저 확인한다.
- 컨트롤러는 `ApiResponse.success/error` 정적 팩토리로만 응답을 만든다.
- 도메인 실패는 `GeneralException` + 전용 `BaseStatus` enum(예: `SpotErrorStatus`)으로 표현한다.
- 엔티티는 `BaseEntity`를 상속한다.
