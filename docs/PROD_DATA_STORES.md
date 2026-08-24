# Production MySQL and Redis Setup

공모전 제출용 Render 배포에서 사용할 운영 MySQL과 Redis 준비 가이드다.
KOMME-BE는 MySQL, Redis, Flyway를 사용하므로 애플리케이션 배포 전에 두 저장소 접속 정보가 먼저 준비되어야 한다.

## Recommended Setup

| Store | Recommended | Reason |
| --- | --- | --- |
| MySQL | Railway MySQL | MySQL 템플릿과 외부 TCP 접속 정보를 빠르게 만들 수 있다. |
| Redis | Upstash Redis | Redis 호환 TCP 접속을 지원하고 TLS가 기본 활성화되어 Render 외부 연결에 적합하다. |

Render 자체도 MySQL private service 배포는 가능하지만, 관리형 MySQL이 아니라 디스크 기반 직접 운영에 가깝다.
공모전 제출용 데모에서는 DB 운영 부담을 줄이는 쪽이 더 안전하다.

## MySQL

### Railway MySQL

1. Railway에서 프로젝트를 생성한다.
2. `+ New` 또는 command menu에서 MySQL database를 추가한다.
3. MySQL 서비스의 Settings > Networking에서 Public Access를 켠다.
4. MySQL 제공자가 IP allow-list 또는 firewall rule을 지원하는지 확인한다.
5. 지원한다면 Render Web Service의 outbound IP range 또는 dedicated outbound IP만 허용한다.
6. 생성된 public 접속 정보를 확인한다.
7. Render 환경변수에 다음 값을 등록한다.

```text
DB_URL=jdbc:mysql://{MYSQL_PUBLIC_HOST}:{MYSQL_PUBLIC_PORT}/{MYSQLDATABASE}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME={MYSQLUSER}
DB_PASSWORD={MYSQLPASSWORD}
```

Railway 내부 변수명은 보통 다음 값을 제공한다.

```text
MYSQLHOST
MYSQLPORT
MYSQLUSER
MYSQLPASSWORD
MYSQLDATABASE
MYSQL_URL
MYSQL_PUBLIC_URL
```

Render에서 Railway MySQL로 접속하려면 Railway public TCP proxy가 필요하다.
private URL은 같은 Railway 프로젝트 내부 서비스용이므로 Render Web Service에서는 사용할 수 없다.

### Connection Checklist

- [ ] MySQL 버전은 로컬 개발 환경과 같은 8.x 계열인지 확인한다.
- [ ] `DB_URL`은 `jdbc:mysql://...` 형식으로 변환한다.
- [ ] DB 이름은 `komme` 또는 운영용으로 확정한 이름과 일치시킨다.
- [ ] public TCP proxy host/port를 사용한다.
- [ ] IP allow-list 또는 firewall rule 지원 여부를 확인한다.
- [ ] 지원 시 Render Web Service outbound IP range 또는 dedicated outbound IP만 허용한다.
- [ ] IP 제한이 불가능하면 강한 비밀번호, 주기적 비밀번호 교체, 접속 로그 확인 일정을 정한다.
- [ ] Flyway migration이 운영 DB에 처음 적용될 수 있도록 빈 DB 또는 호환 스키마를 준비한다.

### Local Connection Test

운영 DB 접속 정보가 준비되면 로컬에서 먼저 TCP 접속을 확인한다.

```text
mysql -h {MYSQL_PUBLIC_HOST} -P {MYSQL_PUBLIC_PORT} -u {MYSQLUSER} -p {MYSQLDATABASE}
```

연결 후 최소한 다음을 확인한다.

```sql
SELECT VERSION();
SHOW DATABASES;
```

## Redis

### Upstash Redis

1. Upstash에서 Redis database를 생성한다.
2. Primary Region은 Render Web Service와 가까운 리전을 선택한다.
3. Redis protocol 접속 정보를 확인한다.
4. Render 환경변수에 다음 값을 등록한다.

```text
REDIS_HOST={UPSTASH_ENDPOINT}
REDIS_PORT={UPSTASH_PORT}
REDIS_PASSWORD={UPSTASH_PASSWORD}
REDIS_SSL_ENABLED=true
```

Upstash Redis는 TLS가 활성화된 Redis 접속을 사용하므로 `REDIS_SSL_ENABLED=true`로 둔다.
REST URL/token은 Spring Data Redis 설정에 바로 맞지 않으므로, Redis protocol endpoint/password를 사용한다.

### Connection Checklist

- [ ] Redis endpoint, port, password를 확보한다.
- [ ] TLS 사용 여부를 확인하고 `REDIS_SSL_ENABLED=true`로 등록한다.
- [ ] Redis는 이메일 인증, refresh token, access token blacklist에 사용되므로 운영 배포 전 반드시 연결을 확인한다.
- [ ] 무료/서버리스 요금제의 command limit, sleep, eviction 정책이 제출 기간 동안 문제 없는지 확인한다.

### Local Connection Test

TLS Redis는 다음처럼 접속을 확인한다.

```text
redis-cli --tls -h {REDIS_HOST} -p {REDIS_PORT} -a {REDIS_PASSWORD} ping
```

정상이라면 다음 응답이 온다.

```text
PONG
```

## Render Environment Mapping

운영 저장소 준비 후 Render에는 최소 다음 값을 등록한다.

| Render Variable | Source |
| --- | --- |
| `DB_URL` | Railway MySQL public TCP host/port/database를 JDBC URL로 변환 |
| `DB_USERNAME` | Railway `MYSQLUSER` |
| `DB_PASSWORD` | Railway `MYSQLPASSWORD` |
| `REDIS_HOST` | Upstash Redis endpoint |
| `REDIS_PORT` | Upstash Redis port |
| `REDIS_PASSWORD` | Upstash Redis password |
| `REDIS_SSL_ENABLED` | Upstash 사용 시 `true` |

전체 Render 환경변수 목록은 `docs/RENDER_ENV_VARS.md`를 따른다.

## Deployment Order

1. MySQL을 먼저 생성하고 public TCP 접속을 연다.
2. Redis를 생성하고 TLS 접속 정보를 확보한다.
3. 로컬에서 MySQL, Redis 접속 테스트를 한다.
4. Render Web Service에 환경변수를 등록한다.
5. Render 배포를 실행한다.
6. Render 로그에서 Flyway migration 성공 여부를 확인한다.
7. `/health`, `/v3/api-docs` 응답을 확인한다.

## Notes

- 운영 DB 비밀번호와 Redis password는 저장소, PR, 이슈 댓글에 남기지 않는다.
- public TCP 접속을 켠 MySQL은 가능하면 Render outbound IP만 접근하도록 제한한다.
- Render Dashboard의 Web Service > Connect > Outbound 탭에서 outbound IP range를 확인한다.
- DB 제공자가 CIDR allow-list를 지원하지 않거나 공유 outbound range 허용이 부담되면 Render dedicated outbound IP 사용을 검토한다.
- IP 제한이 불가능한 요금제라면 강한 비밀번호, 주기적 비밀번호 교체, 접속 로그 모니터링으로 보완하고 그 이유를 이슈/PR에 남긴다.
- 제출 종료 후 public TCP 접근 정책을 다시 점검하거나 비활성화한다.
- Redis 데이터는 토큰/인증 상태를 담으므로 운영 DB와 동일하게 secret으로 취급한다.

## References

- Render Service Types: https://render.com/docs/service-types
- Render Deploy MySQL: https://render.com/docs/deploy-mysql
- Render Outbound IP Addresses: https://render.com/docs/outbound-ip-addresses
- Render Dedicated IPs: https://render.com/docs/dedicated-ips
- Railway MySQL: https://docs.railway.com/databases/mysql
- Railway TCP Proxy: https://docs.railway.com/networking/tcp-proxy
- Upstash Redis Security: https://upstash.com/docs/redis/features/security
- Upstash Redis Getting Started: https://upstash.com/docs/redis/overall/getstarted
