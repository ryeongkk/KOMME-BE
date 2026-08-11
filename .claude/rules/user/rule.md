---
paths:
  - "src/main/java/com/komme/domain/user/**"
---

# user 도메인 규칙

KOMME 사용자 계정/프로필의 단일 진실 소스(`User` 엔티티)를 담당하는 패키지다. 상세 배경/의존 관계는 [[user-agent]] 참고.

- `User`는 이메일/닉네임에 유니크 제약이 걸린 단일 엔티티로 LOCAL/GOOGLE/APPLE 세 provider를 모두 표현한다. provider별 계정 생성은 `User.createLocal`/`User.createOAuth` 정적 팩토리로만 하고, setter 대신 `changePassword`/`changeNickname`/`changePreferredLanguage`/`completeProfile` 같은 의도가 드러나는 행위 메서드로 상태를 바꾼다.
- 닉네임 형식 검증(정규식, 에러 메시지)은 `domain.user.util.NicknamePolicy` 상수로 관리한다. 닉네임을 입력받는 DTO(회원가입, OAuth 프로필 완성, 닉네임 변경, 닉네임 중복 확인)는 전부 이 상수를 참조하며 각자 정규식을 새로 만들지 않는다.
- 이메일/닉네임처럼 유니크 제약이 걸린 필드의 조회는 `UserReader`에 모은다. 다른 도메인(특히 auth)이 사용자 조회가 필요하면 `UserRepository`를 직접 주입받지 말고 `UserReader`(또는 auth 쪽 `AuthUserReader`)를 통해서만 접근한다 — 같은 조회 로직이 여러 곳에 흩어지면 정책이 어긋나기 쉽다.
- 유니크 제약 위반은 사전 존재-확인(`exists...`)만으로 끝내지 않고, 실제 저장/flush 시점의 `DataIntegrityViolationException`도 `UserConstraintExceptionMapper`로 감싸 `GeneralException`+`UserErrorStatus`로 변환한다 (동시 요청으로 인한 race condition 대비).
- 도메인 실패는 `GeneralException` + 전용 `BaseStatus` enum(`UserErrorStatus`)으로 표현한다. auth 쪽에서 발생하는 같은 종류의 실패(예: 회원가입 중 닉네임 중복)는 `AuthErrorStatus`를 쓰고, user 쪽 자체 액션(프로필/닉네임 변경)은 `UserErrorStatus`를 쓴다 — 실패를 감지한 도메인의 상태 enum을 쓴다는 원칙을 따른다.
- 컨트롤러는 `ApiResponse.success/error` 정적 팩토리로만 응답을 만든다.
- 엔티티는 `BaseEntity`를 상속한다.
- 약관 동의(필수/선택 모두)는 서버에 기록을 남기지 않는다 — 프론트에서만 관리한다. `TermsAgreement`/`TermsAgreementService`/`TermsType` 및 관련 API(`/api/v1/auth/terms`, `/api/v1/users/me/terms/{type}`)는 존재하지 않는다. 나중에 마케팅/푸시 알림처럼 서버가 실제로 발송 여부를 판단해야 하는 동의가 다시 필요해지면, 그건 새로 설계할 사안이지 이전 구조를 되살리는 게 아니다.
- auth 도메인은 이 도메인(`User` 엔티티, `UserReader`, `NicknamePolicy` 등)에 의존한다 — 반대 방향(user가 auth에 의존)은 만들지 않는다.
