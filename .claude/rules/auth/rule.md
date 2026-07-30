---
paths:
  - "src/main/java/com/komme/domain/auth/**"
---

# auth 도메인 규칙

KOMME의 로그인/회원가입(이메일, 구글, 애플)을 담당하는 패키지다. 상세 배경/의존 관계는 [[auth-agent]] 참고.

- 로그인 수단은 이메일/구글/애플 3가지다. `provider`(LOCAL/GOOGLE/APPLE) + provider별 고유 식별자로 계정을 구분한다.
- 이메일 비밀번호는 반드시 해시(BCrypt 등)해서 저장한다. 평문 저장·로깅 금지.
- 이메일 인증 Redis 키는 목적(`SIGN_UP`, `PASSWORD_RESET`)별로 분리한다. 회원가입은 인증 완료 플래그를 사용하고, 비밀번호 재설정은 인증 성공 시 짧은 TTL의 일회성 reset token을 발급해 `token -> email` 매핑으로만 최종 변경을 허용한다.
- 비밀번호 재설정은 가입된 `LOCAL` 계정에만 허용하고, 성공 시 모든 Refresh Token을 폐기한다.
- 구글/애플 로그인은 클라이언트가 보낸 사용자 정보를 그대로 신뢰하지 않고, 서버에서 provider의 idToken/identity token을 검증한 결과만 사용한다.
- 애플은 이메일이 최초 로그인 시에만 제공될 수 있다 — 이후 로그인에서 이메일이 없는 케이스를 처리한다.
- JWT 시크릿, OAuth 클라이언트 시크릿 등은 `application.yaml`/환경변수로 주입한다. 코드나 커밋에 하드코딩하지 않는다.
- 동일 이메일의 다른 provider 가입/로그인 처리(계정 병합 여부)는 임의로 정하지 않고 구현 전에 사용자와 정책을 확인한다.
- 컨트롤러는 `ApiResponse.success/error` 정적 팩토리로만 응답을 만든다.
- 도메인 실패는 `GeneralException` + 전용 `BaseStatus` enum(예: `AuthErrorStatus`)으로 표현한다.
- 엔티티는 `BaseEntity`를 상속한다.
