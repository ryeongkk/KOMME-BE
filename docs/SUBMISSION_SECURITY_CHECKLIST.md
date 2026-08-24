# Submission Security Checklist

공모전 제출 전 민감값 노출과 운영 설정 실수를 줄이기 위한 보안 점검 목록이다.

## Git Tracking

- [ ] `application-local.yaml`이 Git 추적 대상이 아닌지 확인한다.
- [ ] `.env`, `.env.*` 파일이 Git 추적 대상이 아닌지 확인한다.
- [ ] 운영 secret은 `application-prod.yaml`에 직접 쓰지 않고 `${ENV_VAR}` placeholder로만 둔다.
- [ ] 제출용 압축 파일을 만들 경우 `.git`, `.gradle`, `build`, `.idea`, `application-local.yaml`, `.env*`를 제외한다.

확인 명령:

```text
git ls-files src/main/resources/application-local.yaml
git ls-files '.env*'
```

위 명령은 아무것도 출력되지 않아야 한다.

## Secret Scan

커밋 전 추적 파일에 명백한 secret 문자열이 들어갔는지 확인한다.

```text
git grep -n -I -E "(PRIVATE KEY|BEGIN|sk-|AIza|password:|secret:|service-key:|rest-api-key:)"
```

허용 가능한 항목:

- 테스트 전용 값
- 문서의 placeholder 예시
- `${ENV_VAR}` placeholder

실제 운영값처럼 보이는 항목이 있으면 커밋하지 않고 값을 제거하거나 재발급한다.

## Local Secrets

- [ ] 로컬 `application-local.yaml`의 메일 앱 비밀번호, Kakao API key, Tour API key가 외부에 노출된 적 없는지 확인한다.
- [ ] 화면 공유, PR diff, 제출 파일, 이슈 댓글에 로컬 설정 파일 내용이 포함되지 않았는지 확인한다.
- [ ] 이미 노출된 가능성이 있으면 앱 비밀번호/API key를 재발급한다.

## Render Environment

- [ ] Render 환경변수는 `docs/RENDER_ENV_VARS.md` 기준으로 등록한다.
- [ ] `SPRING_PROFILES_ACTIVE=prod`인지 확인한다.
- [ ] `JWT_SECRET`은 충분히 긴 운영 전용 값으로 등록한다.
- [ ] `CORS_ALLOWED_ORIGINS`에는 실제 프론트엔드 origin만 등록한다.
- [ ] Render 로그에 secret 값이 출력되지 않는지 확인한다.

## Data Stores

- [ ] 운영 MySQL/Redis 준비 절차는 `docs/PROD_DATA_STORES.md` 기준으로 진행한다.
- [ ] MySQL public TCP 접근은 가능하면 Render outbound IP로 제한한다.
- [ ] IP 제한이 불가능하면 강한 비밀번호, 비밀번호 교체 계획, 접속 로그 확인 계획을 남긴다.
- [ ] Redis password와 TLS 설정을 확인한다.

## After Submission

- [ ] 제출 종료 후 public TCP DB 접근을 비활성화하거나 접근 정책을 재점검한다.
- [ ] 공모전 데모용 메일/API key/JWT secret을 운영 장기 사용 값과 분리한다.
- [ ] 더 이상 필요 없는 Render/Railway/Upstash 리소스를 중지하거나 삭제한다.
