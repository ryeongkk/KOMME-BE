---
paths:
  - "src/main/java/com/komme/domain/i18n/**"
---

# i18n 도메인 규칙

KOMME의 다국어 지원(UI 문자열, 스팟/관광지 정보, 체험 가이드 본문 번역)을 담당하는 패키지다. 상세 배경/의존 관계는 [[i18n-agent]] 참고.

- MVP 지원 언어는 영어/일본어/중국어(간체) 3개이며, 지원 언어 코드는 `Language` enum으로 관리한다.
- 번역 데이터는 자체 번역 로직 없이 `com.komme.tourapi`의 다국어 관광정보 API를 통해서만 가져온다.
- 지원하지 않는 언어 코드 요청은 조용히 기본 언어로 폴백하지 않고, 명확한 에러(`GeneralException` + 전용 `BaseStatus`)로 응답한다.
