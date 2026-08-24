# KOMME API 명세서

## 공통

- Base URL: `{server-origin}`
- 모든 엔드포인트 경로는 `/api/v1` 접두사를 포함한 전체 경로로 표기한다.
- 인증 방식: `Authorization: Bearer {accessToken}`
- Content-Type: `application/json`
- 성공 응답 공통 코드: `COM_200`

### 공통 응답 형식

```json
{
  "isSuccess": true,
  "code": "COM_200",
  "message": "성공적으로 처리되었습니다.",
  "data": {}
}
```

데이터가 없는 성공 응답은 `data` 필드가 제외된다.

```json
{
  "isSuccess": true,
  "code": "COM_200",
  "message": "성공적으로 처리되었습니다."
}
```

실패 응답 형식:

```json
{
  "isSuccess": false,
  "code": "AUTH_401_2",
  "message": "유효하지 않은 토큰입니다."
}
```

### 공통 입력 규칙

| 항목 | 규칙 |
| --- | --- |
| 비밀번호 | 8~20자, 영문/숫자/특수문자를 모두 포함, ASCII 출력 가능 문자 범위 |
| 닉네임 | 2~20자, 영문과 숫자만 허용 |
| 인증 코드 | 6자리 숫자 |
| 선호 언어 | `ENGLISH`, `JAPANESE`, `CHINESE_SIMPLIFIED` |
| 코스 주제 | `FOOD`, `HEALING`, `EXPLORATION` |
| 방문 장소 개수 | `TWO`, `THREE`, `FOUR_OR_MORE` |
| 코스 목록 상태 | `UPCOMING`, `HISTORY` |

### 인증이 필요 없는 API

- `POST /api/v1/auth/email-verifications/send`
- `POST /api/v1/auth/email-verifications/confirm`
- `POST /api/v1/auth/password-resets/email-verifications/send`
- `POST /api/v1/auth/password-resets/email-verifications/confirm`
- `PATCH /api/v1/auth/password-resets`
- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/oauth/apple`
- `POST /api/v1/auth/oauth/google`
- `POST /api/v1/auth/tokens/reissue`
- `GET /api/v1/users/nicknames/availability`

---

## Auth

### 이메일 인증 코드 전송

가입되지 않은 이메일로 6자리 인증 코드를 전송한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/email-verifications/send` |
| Auth | 불필요 |

Request Body:

```json
{
  "email": "user@example.com"
}
```

Response `200`:

```json
{
  "isSuccess": true,
  "code": "COM_200",
  "message": "성공적으로 처리되었습니다."
}
```

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 409 | `AUTH_409_1` | 이미 가입된 이메일입니다. |
| 429 | `AUTH_429_1` | 인증 코드 입력 횟수를 초과했습니다. |
| 429 | `AUTH_429_2` | 잠시 후 인증 이메일을 다시 요청해 주세요. |
| 500 | `AUTH_500_1` | 인증 이메일 전송에 실패했습니다. |

### 이메일 인증 코드 확인

전송된 인증 코드를 확인하고 이메일 인증 상태를 저장한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/email-verifications/confirm` |
| Auth | 불필요 |

Request Body:

```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `AUTH_400_1` | 이메일 인증 코드가 올바르지 않습니다. |
| 400 | `AUTH_400_2` | 이메일 인증 코드가 만료되었습니다. |
| 429 | `AUTH_429_1` | 인증 코드 입력 횟수를 초과했습니다. |

### 비밀번호 재설정 인증 코드 전송

가입된 `LOCAL` 계정인 경우에만 비밀번호 재설정 인증 코드를 발송한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/password-resets/email-verifications/send` |
| Auth | 불필요 |

Request Body:

```json
{
  "email": "user@example.com"
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 429 | `AUTH_429_1` | 인증 코드 입력 횟수를 초과했습니다. |
| 429 | `AUTH_429_2` | 잠시 후 인증 이메일을 다시 요청해 주세요. |
| 500 | `AUTH_500_1` | 인증 이메일 전송에 실패했습니다. |

### 비밀번호 재설정 인증 코드 확인

비밀번호 재설정 인증 코드를 확인하고 일회성 reset token을 발급한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/password-resets/email-verifications/confirm` |
| Auth | 불필요 |

Request Body:

```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

Response `200`:

```json
{
  "isSuccess": true,
  "code": "COM_200",
  "message": "성공적으로 처리되었습니다.",
  "data": {
    "resetToken": "w5ME7pKqBlj8xj-DaYcpCV8RpYa70PsTv_oak93wxqs"
  }
}
```

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `AUTH_400_1` | 이메일 인증 코드가 올바르지 않습니다. |
| 400 | `AUTH_400_2` | 이메일 인증 코드가 만료되었습니다. |
| 429 | `AUTH_429_1` | 인증 코드 입력 횟수를 초과했습니다. |

### 비밀번호 재설정

일회성 reset token을 검증하고 새 비밀번호로 변경한다. 성공 시 모든 Refresh Token이 폐기된다.

| 항목 | 값 |
| --- | --- |
| Method | `PATCH` |
| URL | `/api/v1/auth/password-resets` |
| Auth | 불필요 |

Request Body:

```json
{
  "resetToken": "w5ME7pKqBlj8xj-DaYcpCV8RpYa70PsTv_oak93wxqs",
  "newPassword": "Password1!"
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 401 | `AUTH_401_6` | 유효하지 않은 비밀번호 재설정 토큰입니다. |

### 이메일 회원가입

인증이 완료된 이메일과 비밀번호, 닉네임으로 `LOCAL` 계정을 생성한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/signup` |
| Auth | 불필요 |

Request Body:

```json
{
  "email": "user@example.com",
  "password": "Password1!",
  "nickname": "komme01"
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 403 | `AUTH_403_1` | 이메일 인증이 필요합니다. |
| 409 | `AUTH_409_1` | 이미 가입된 이메일입니다. |
| 409 | `AUTH_409_2` | 이미 사용 중인 닉네임입니다. |
| 409 | `AUTH_409_4` | 탈퇴 후 7일간 재가입할 수 없습니다. |

### 이메일 로그인

`LOCAL` 계정의 이메일과 비밀번호를 검증하고 Access Token과 Refresh Token을 발급한다. 요청한 선호 언어로 사용자의 `preferredLanguage`를 최신화한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/login` |
| Auth | 불필요 |

Request Body:

```json
{
  "email": "user@example.com",
  "password": "Password1!",
  "preferredLanguage": "ENGLISH"
}
```

Response `200`:

```json
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
```

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 401 | `AUTH_401_1` | 이메일 또는 비밀번호가 올바르지 않습니다. |

### 토큰 재발급

유효한 Refresh Token을 검증하고 새로운 Access Token과 Refresh Token을 발급한다. 기존 Refresh Token은 즉시 폐기된다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/tokens/reissue` |
| Auth | 불필요 |

Request Body:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Response `200`:

```json
{
  "isSuccess": true,
  "code": "COM_200",
  "message": "성공적으로 처리되었습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 401 | `AUTH_401_3` | 만료된 토큰입니다. |

### 비밀번호 변경

현재 비밀번호를 확인하고 새 비밀번호로 변경한다. 성공 시 모든 Refresh Token이 폐기된다.

| 항목 | 값 |
| --- | --- |
| Method | `PATCH` |
| URL | `/api/v1/auth/password` |
| Auth | 필요 |

Request Body:

```json
{
  "currentPassword": "Password1!",
  "newPassword": "NewPassword1!"
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 400 | `AUTH_400_3` | 현재 비밀번호가 올바르지 않습니다. |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 401 | `AUTH_401_3` | 만료된 토큰입니다. |

### 로그아웃

현재 Access Token을 블랙리스트에 등록하고 요청한 기기의 Refresh Token을 폐기한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/logout` |
| Auth | 필요 |

Request Body:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 401 | `AUTH_401_3` | 만료된 토큰입니다. |

### 계정 탈퇴

인증된 사용자의 계정을 하드 삭제하고 모든 Refresh Token과 현재 Access Token을 폐기한다. 탈퇴 후 7일간 동일 이메일로 재가입할 수 없다.

| 항목 | 값 |
| --- | --- |
| Method | `DELETE` |
| URL | `/api/v1/auth/withdraw` |
| Auth | 필요 |

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 401 | `AUTH_401_3` | 만료된 토큰입니다. |
| 500 | `AUTH_500_2` | 계정 탈퇴에 실패했습니다. |

---

## OAuth

### Apple 로그인

Apple identity token의 서명, 발급자, 대상, 만료를 검증한다. 연결된 계정은 로그인하고, 검증된 이메일의 기존 계정은 Apple 계정을 연결하며, 가입 이력이 없으면 최소 프로필의 Apple 계정을 생성한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/oauth/apple` |
| Auth | 불필요 |

Request Body:

```json
{
  "identityToken": "eyJraWQiOiJ...",
  "preferredLanguage": "ENGLISH"
}
```

Response `200`: `LoginResponse`

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 400 | `AUTH_400_5` | Apple 계정의 이메일 정보가 필요합니다. |
| 401 | `AUTH_401_4` | 유효하지 않은 Apple identity token입니다. |
| 409 | `AUTH_409_3` | 이미 연결된 OAuth 계정입니다. |
| 502 | `AUTH_502_1` | Apple 인증 서버 연결에 실패했습니다. |

### Google 로그인

Google ID token의 서명, 발급자, 대상, 만료를 검증한다. 연결된 계정은 로그인하고, 검증된 이메일의 기존 계정은 Google 계정을 연결하며, 가입 이력이 없으면 최소 프로필의 Google 계정을 생성한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/auth/oauth/google` |
| Auth | 불필요 |

Request Body:

```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIs...",
  "preferredLanguage": "ENGLISH"
}
```

Response `200`: `LoginResponse`

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 400 | `AUTH_400_6` | Google 계정의 이메일 정보가 필요합니다. |
| 401 | `AUTH_401_5` | 유효하지 않은 Google identity token입니다. |
| 409 | `AUTH_409_3` | 이미 연결된 OAuth 계정입니다. |
| 502 | `AUTH_502_2` | Google 인증 서버 연결에 실패했습니다. |

### 소셜 로그인 사용자 프로필 완성

Apple 또는 Google 최초 로그인 후 비어 있는 프로필 정보를 저장한다.

| 항목 | 값 |
| --- | --- |
| Method | `PATCH` |
| URL | `/api/v1/auth/oauth/profile` |
| Auth | 필요 |

Request Body:

```json
{
  "nickname": "komme01"
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 409 | `AUTH_409_2` | 이미 사용 중인 닉네임입니다. |

---

## User

### 마이페이지 프로필 조회

인증된 사용자의 닉네임, 연결 계정, 선호 언어, 위치 정보 동의 상태를 조회한다.

| 항목 | 값 |
| --- | --- |
| Method | `GET` |
| URL | `/api/v1/users/me` |
| Auth | 필요 |

Response `200`:

```json
{
  "isSuccess": true,
  "code": "COM_200",
  "message": "성공적으로 처리되었습니다.",
  "data": {
    "nickname": "komme01",
    "provider": "LOCAL",
    "preferredLanguage": "ENGLISH",
    "locationConsentAgreed": false
  }
}
```

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |

### 마이페이지 닉네임 변경

인증된 사용자의 닉네임을 변경한다. 본인의 기존 닉네임은 중복으로 보지 않는다.

| 항목 | 값 |
| --- | --- |
| Method | `PATCH` |
| URL | `/api/v1/users/me/nickname` |
| Auth | 필요 |

Request Body:

```json
{
  "nickname": "komme02"
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 409 | `USER_409_1` | 이미 사용 중인 닉네임입니다. |

### 닉네임 사용 가능 여부 조회

회원가입 또는 닉네임 변경 전에 닉네임 중복 여부를 조회한다.

| 항목 | 값 |
| --- | --- |
| Method | `GET` |
| URL | `/api/v1/users/nicknames/availability?nickname={nickname}` |
| Auth | 불필요 |

Query Params:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| nickname | string | Y | 2~20자의 영문과 숫자 |

Response `200`:

```json
{
  "isSuccess": true,
  "code": "COM_200",
  "message": "성공적으로 처리되었습니다.",
  "data": {
    "available": true
  }
}
```

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |

### 마이페이지 선호 언어 변경

인증된 사용자의 선호 언어를 변경한다.

| 항목 | 값 |
| --- | --- |
| Method | `PATCH` |
| URL | `/api/v1/users/me/language` |
| Auth | 필요 |

Request Body:

```json
{
  "preferredLanguage": "JAPANESE"
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |

### 마이페이지 위치 정보 동의 변경

인증된 사용자의 위치 정보 수집/이용 동의 상태를 변경한다.

| 항목 | 값 |
| --- | --- |
| Method | `PATCH` |
| URL | `/api/v1/users/me/location-consent` |
| Auth | 필요 |

Request Body:

```json
{
  "agreed": true
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |

---

## Course

### 코스 생성

지역 키워드로 중심 좌표를 찾고 주변에서 주제에 맞는 스팟을 모아 시간대별 하루 코스를 생성한다. 서울/부산 지역만 지원한다.

| 항목 | 값 |
| --- | --- |
| Method | `POST` |
| URL | `/api/v1/courses` |
| Auth | 필요 |

Request Body:

```json
{
  "regionKeyword": "성동구",
  "topics": ["FOOD"],
  "spotCount": "THREE",
  "visitDate": "2026-08-10"
}
```

Response `200`:

```json
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
```

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 400 | `COURSE_400_1` | 서울/부산 지역만 지원합니다. |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 404 | `COURSE_404_1` | 코스를 구성할 스팟이 부족합니다. |
| 404 | `COURSE_404_3` | 해당 지역을 찾을 수 없습니다. |
| 502 | `COURSE_502_1` | 카카오 로컬 API 응답이 올바르지 않습니다. |
| 502 | `COURSE_502_2` | 카카오 로컬 API 연결에 실패했습니다. |

### 코스 목록 조회

인증된 사용자가 저장한 코스 목록을 조회한다. 저장하지 않은 코스는 목록에 노출되지 않는다.

| 항목 | 값 |
| --- | --- |
| Method | `GET` |
| URL | `/api/v1/courses?status={status}` |
| Auth | 필요 |

Query Params:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| status | enum | Y | `UPCOMING` 또는 `HISTORY` |

Response `200`:

```json
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
```

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |

### 코스 상세 조회

코스의 저장 제목과 스팟 타임라인을 포함한 상세 정보를 조회한다. 본인 코스가 아니면 존재 여부를 숨기기 위해 404로 응답한다.

| 항목 | 값 |
| --- | --- |
| Method | `GET` |
| URL | `/api/v1/courses/{courseId}` |
| Auth | 필요 |

Path Params:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| courseId | number | Y | 코스 ID |

Response `200`: `CourseDetailResponse`

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 404 | `COURSE_404_2` | 코스를 찾을 수 없습니다. |

### 코스 삭제

코스를 하드 삭제한다. 본인 코스가 아니면 존재 여부를 숨기기 위해 404로 응답한다.

| 항목 | 값 |
| --- | --- |
| Method | `DELETE` |
| URL | `/api/v1/courses/{courseId}` |
| Auth | 필요 |

Path Params:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| courseId | number | Y | 코스 ID |

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 404 | `COURSE_404_2` | 코스를 찾을 수 없습니다. |

### 코스 저장

생성된 코스에 사용자가 입력한 이름을 붙여 저장한다. 저장해야 코스 목록에 노출된다. 이미 저장한 코스를 다시 저장하면 이름만 갱신된다.

| 항목 | 값 |
| --- | --- |
| Method | `PATCH` |
| URL | `/api/v1/courses/{courseId}/save` |
| Auth | 필요 |

Path Params:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| courseId | number | Y | 코스 ID |

Request Body:

```json
{
  "title": "성동구 음식 Day"
}
```

Response `200`: 데이터 없는 성공 응답

Errors:

| HTTP | Code | Message |
| --- | --- | --- |
| 400 | `COM_400` | 잘못된 요청입니다. |
| 401 | `AUTH_401_2` | 유효하지 않은 토큰입니다. |
| 404 | `COURSE_404_2` | 코스를 찾을 수 없습니다. |

---

## 응답 DTO 요약

### LoginResponse

| Field | Type | Description |
| --- | --- | --- |
| accessToken | string | Access Token |
| refreshToken | string | Refresh Token |
| profileCompleted | boolean | OAuth 최소 프로필 사용자의 프로필 완성 여부 |

### TokenReissueResponse

| Field | Type | Description |
| --- | --- | --- |
| accessToken | string | 새 Access Token |
| refreshToken | string | 새 Refresh Token |

### PasswordResetTokenResponse

| Field | Type | Description |
| --- | --- | --- |
| resetToken | string | 비밀번호 재설정용 일회성 토큰 |

### UserProfileResponse

| Field | Type | Description |
| --- | --- | --- |
| nickname | string | 닉네임 |
| provider | enum | `LOCAL`, `APPLE`, `GOOGLE` |
| preferredLanguage | enum | `ENGLISH`, `JAPANESE`, `CHINESE_SIMPLIFIED` |
| locationConsentAgreed | boolean | 위치 정보 수집/이용 동의 여부 |

### NicknameAvailabilityResponse

| Field | Type | Description |
| --- | --- | --- |
| available | boolean | 닉네임 사용 가능 여부 |

### CourseDetailResponse

| Field | Type | Description |
| --- | --- | --- |
| courseId | number | 코스 ID |
| title | string/null | 사용자가 저장한 코스명, 저장 전 생성 응답에서는 null 가능 |
| regionName | string | 지역명 |
| topics | enum array | 코스 주제 목록 |
| visitDate | date | 방문 날짜 |
| spots | array | 코스 스팟 목록 |

### CourseSpotResponse

| Field | Type | Description |
| --- | --- | --- |
| spotId | number | 스팟 ID |
| name | string | 스팟명 |
| sequence | number | 코스 내 순서 |
| timeSlot | enum | `MORNING`, `LUNCH`, `EVENING`, `ANYTIME` |
| latitude | number | 위도 |
| longitude | number | 경도 |
| thumbnailUrl | string | 썸네일 URL |
| distanceToNextMeters | number/null | 다음 스팟까지 거리, 마지막 스팟은 null 가능 |

### CourseSummaryResponse

| Field | Type | Description |
| --- | --- | --- |
| courseId | number | 코스 ID |
| title | string | 사용자가 저장한 코스명 |
| regionName | string | 지역명 |
| visitDate | date | 방문 날짜 |
| topics | enum array | 코스 주제 목록 |
| spotCount | number | 코스에 포함된 스팟 개수 |

---

## 확인 필요 메모

- `CourseErrorStatus` enum 선언 순서는 HTTP 코드 오름차순이 아니지만, 명세의 에러 표는 HTTP 코드 기준으로 정리했다.
