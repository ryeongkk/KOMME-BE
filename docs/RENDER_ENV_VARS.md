# Render Environment Variables

Render Web Service에서 `SPRING_PROFILES_ACTIVE=prod`로 실행할 때 필요한 환경변수 목록이다.
실제 비밀번호, API 키, JWT secret은 저장소에 커밋하지 말고 Render Dashboard의 Environment 탭에만 등록한다.

## Required

| Name | Example | Description |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` | 운영 프로필 |
| `DB_URL` | `jdbc:mysql://host:3306/komme?serverTimezone=Asia/Seoul&characterEncoding=UTF-8` | 운영 MySQL JDBC URL |
| `DB_USERNAME` | `komme` | 운영 DB 계정 |
| `DB_PASSWORD` | `secret` | 운영 DB 비밀번호 |
| `REDIS_HOST` | `host` | 운영 Redis host |
| `REDIS_PORT` | `6379` | 운영 Redis port |
| `REDIS_PASSWORD` | `secret` | 운영 Redis 비밀번호 |
| `REDIS_SSL_ENABLED` | `true` | Redis TLS 사용 여부 |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | `user@example.com` | SMTP 계정 |
| `MAIL_PASSWORD` | `secret` | SMTP 앱 비밀번호 |
| `MAIL_SENDER` | `KOMME <user@example.com>` | 인증 메일 발신자 |
| `JWT_SECRET` | `at-least-32-byte-secret-value` | JWT 서명 secret |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `1h` | Access Token 만료 시간 |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `14d` | Refresh Token 만료 시간 |
| `APPLE_CLIENT_ID` | `com.komme.app` | Apple OAuth client id |
| `APPLE_JWKS_CACHE_TTL` | `1h` | Apple JWKS cache TTL |
| `GOOGLE_CLIENT_ID` | `client-id.apps.googleusercontent.com` | Google OAuth client id |
| `GOOGLE_JWKS_CACHE_TTL` | `1h` | Google JWKS cache TTL |
| `EMAIL_VERIFICATION_CODE_EXPIRATION` | `3m` | 이메일 인증 코드 만료 시간 |
| `EMAIL_VERIFICATION_VERIFIED_EXPIRATION` | `30m` | 이메일 인증 완료 플래그 만료 시간 |
| `EMAIL_VERIFICATION_MAX_ATTEMPTS` | `5` | 인증 코드 최대 입력 횟수 |
| `EMAIL_VERIFICATION_RESEND_COOLDOWN` | `1m` | 인증 메일 재발송 대기 시간 |
| `EMAIL_VERIFICATION_LOCK_EXPIRATION` | `5m` | 인증 실패 잠금 시간 |
| `TOUR_API_SERVICE_KEY` | `secret` | 한국관광공사 API service key |
| `KAKAO_REST_API_KEY` | `secret` | Kakao REST API key |
| `CORS_ALLOWED_ORIGINS` | `https://frontend.example.com` | 허용할 프론트엔드 origin |

## Optional

| Name | Example | Description |
| --- | --- | --- |
| `PORT` | `8080` | Render가 자동 주입한다. 직접 지정하지 않으면 기본값 `8080`을 사용한다. |
| `JAVA_OPTS` | `-Xms256m -Xmx512m` | Docker 실행 시 JVM 옵션을 추가할 때 사용한다. |

## CORS

`CORS_ALLOWED_ORIGINS`에는 브라우저에서 API를 호출할 프론트엔드 origin만 등록한다.
여러 origin이 필요하면 쉼표로 구분한다.

```text
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://frontend.example.com
```

현재 인증 방식은 쿠키가 아니라 `Authorization` 헤더 기반 JWT이므로 CORS credentials는 허용하지 않는다.

## Redis TLS

Upstash, Redis Cloud 같은 외부 Redis는 TLS를 요구하는 경우가 많다. 이 경우 다음처럼 등록한다.

```text
REDIS_SSL_ENABLED=true
```

로컬 Docker Redis처럼 TLS가 없는 환경에서는 다음처럼 등록한다.

```text
REDIS_SSL_ENABLED=false
```

## Render Setup

1. Render Web Service의 Environment 탭을 연다.
2. 위 Required 값을 모두 등록한다.
3. `SPRING_PROFILES_ACTIVE`가 반드시 `prod`인지 확인한다.
4. Health Check Path는 `/health`로 등록한다.
5. 배포 후 Swagger 또는 API 문서 경로로 응답을 확인한다.

```text
https://{render-service-domain}/v3/api-docs
https://{render-service-domain}/swagger-ui.html
```
