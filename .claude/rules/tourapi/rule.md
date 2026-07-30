---
paths:
  - "src/main/java/com/komme/domain/tourapi/**"
---

# tourapi 도메인 규칙

한국관광공사 OpenAPI(KorService2, 연관 관광지, 관광지 집중률, 다국어 관광정보) 연동을 격리하는 패키지다. 상세 배경/의존 관계는 [[tourapi-agent]] 참고.

- 이 패키지는 다른 도메인(`course`, `spot`, `i18n`)에 의존되는 쪽이다. 반대로 이 패키지가 그 도메인들을 import 하지 않는다 (레이어 역전 금지).
- 외부 API 원본 응답 스키마를 그대로 상위 도메인에 반환하지 않는다. 소비하기 쉬운 내부 DTO로 변환해서 제공한다.
- 서비스키 등 인증 정보는 `application.yaml`/환경변수로 주입한다. 코드나 커밋에 하드코딩하지 않는다.
- 외부 API 실패/타임아웃은 이 계층에서 잡아 `GeneralException` + 전용 `BaseStatus`로 변환한다. 원본 예외를 그대로 상위로 흘려보내지 않는다.
- 재시도/폴백 정책을 새로 도입하기 전에 사용자와 먼저 범위를 확인한다.
