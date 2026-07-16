# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Collaboration workflow

When the user proposes a feature idea or requirement, do not edit code immediately.

1. First explain: the feature's purpose/requirements, a recommended plan and flow, the code approach, impact on the existing structure, viable alternatives with trade-offs, and expected edge cases/test scope.
2. Present the necessary choices and wait for the user's decision.
3. Only modify files once the user explicitly says to execute (e.g. "작성해줘", "만들어줘", "구현해줘", "수정해줘", "적용해줘").
4. Requests like "분석해줘", "어떻게 하면 좋을까?", "방향을 알려줘" mean read/analyze only — no file changes.

## Project

KOMME-BE is a Spring Boot 4.0.7 (Java 21) REST API backend, built with Gradle. Root package: `com.komme`.

## Commands

```bash
./gradlew build            # full build (compiles + runs tests)
./gradlew test             # run all tests
./gradlew test --tests "com.komme.KommeBeTempApplicationTests"   # run a single test class
./gradlew test --tests "com.komme.KommeBeTempApplicationTests.contextLoads"   # run a single test method
./gradlew bootRun           # run the app locally
```

Note: `contextLoads()` currently fails without a configured test DataSource — the test environment DB setup is a known follow-up (see PR #2).

## Architecture

### Common/shared layer (`com.komme.common`)

The codebase centers on a shared response/exception convention that all feature code is expected to use:

- **`common.base.BaseEntity`** — `@MappedSuperclass` with `@CreatedDate`/`@LastModifiedDate` (JPA Auditing via `AuditingEntityListener`). All JPA entities should extend this instead of declaring their own timestamp columns. Auditing is enabled globally via `@EnableJpaAuditing` on `KommeBeTempApplication`.
- **`common.base.status.BaseStatus`** — interface (`getHttpStatus()`, `getCode()`, `getMessage()`) implemented by enums that describe a response status.
  - `SuccessStatus` — success cases (currently one generic `COMMON_SUCCESS_STATUS`).
  - `ErrorStatus` — error cases, coded `COM_4xx`/`COM_5xx`. Add new domain-specific status enums (implementing `BaseStatus`) rather than growing `ErrorStatus`/`SuccessStatus` indefinitely as features are added.
- **`common.response.ApiResponse<T>`** — the single response envelope shape (`isSuccess`, `code`, `message`, `data`), serialized in that field order via `@JsonPropertyOrder`. Controllers build responses with the static factories `ApiResponse.success(status)`, `ApiResponse.success(status, data)`, `ApiResponse.error(status)`, `ApiResponse.error(status, message)` — always returning `ResponseEntity<ApiResponse<T>>`. Don't construct raw `ResponseEntity` bodies by hand in controllers.
- **`common.exception.GeneralException`** — the one custom runtime exception type, carrying a `BaseStatus`. Business/domain code should throw `new GeneralException(ErrorStatus.XXX)` (or a domain-specific `BaseStatus` enum) instead of ad-hoc exceptions.
- **`common.exception.GeneralExceptionAdvice`** — global `@RestControllerAdvice` (extends `ResponseEntityExceptionHandler`) that converts exceptions into `ApiResponse` error bodies:
  - `GeneralException` → its own `BaseStatus`'s HTTP status/code/message (5xx logged as `error`, others as `warn`)
  - `IllegalArgumentException` → 400
  - `MethodArgumentNotValidException` (`@Valid` failures) → 400, with the first field/global validation error message extracted
  - `NullPointerException` and anything else uncaught → 500

When adding a new feature area, follow this existing pattern: entities extend `BaseEntity`; failures are signaled via `GeneralException` + a `BaseStatus` enum (add one per domain rather than overloading `ErrorStatus`); controllers return `ApiResponse` via the static factory methods so error handling stays centralized in `GeneralExceptionAdvice`.

### Planned domain packages

KOMME-BE implements the "2026 관광데이터 활용 공모전" proposal: a daily-course curation service for foreign visitors ("한국인처럼 살아보기"). No domain packages exist under `com.komme` yet beyond `common`, but the planned split — and the conventions each one should follow — is documented per domain in `.claude/agents/{domain}/agent.md` (specialized subagents) and `.claude/rules/{domain}/rule.md` (path-scoped rules that auto-load once `src/main/java/com/komme/{domain}/**` exists):

- **`course`** — 맞춤형 데일리 코스 생성, 즉시 체험 추천 (지역/주제/체류시간 기반 시간대별 코스)
- **`spot`** — 관광지/장소(스팟) 도메인 모델, 카테고리, 위치 기반 조회
- **`tourapi`** — 한국관광공사 OpenAPI 연동 계층 (KorService2, 연관 관광지, 집중률, 다국어 관광정보) — 다른 도메인이 의존하는 쪽이며 반대 방향 의존은 금지
- **`i18n`** — 다국어 지원 (영/일/중 우선, 향후 8개 언어)
- **`auth`** — 로그인/회원가입 (이메일, 구글, 애플 3가지 provider). MVP 핵심 기능(course/spot/i18n)은 이 도메인에 의존하지 않으며, 2단계 개인화/UGC 기능부터 의존 관계가 생길 예정

## Git workflow

Branch, issue, PR, and commit conventions are encoded as slash commands in `.claude/commands/` (`/branch`, `/issue`, `/pr`, `/commit`) — these derive naming/templates from `.github/ISSUE_TEMPLATE/ISSUE.md` and `.github/PULL_REQUEST_TEMPLATE.md` and should be used instead of ad hoc naming:

- Branches: `{type}/#{issue-number}-{description}` (e.g. `feat/#1-core-setup`)
- Commits: `{type}: {한글 요약}` (e.g. `feat: BaseEntity 추가`, `refactor: GeneralException 수정`)
- PRs target `develop`

`type` is one of `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`.
