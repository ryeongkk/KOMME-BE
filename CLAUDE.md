# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

## Git workflow

Branch, issue, PR, and commit conventions are encoded as slash commands in `.claude/commands/` (`/branch`, `/issue`, `/pr`, `/commit`) — these derive naming/templates from `.github/ISSUE_TEMPLATE/ISSUE.md` and `.github/PULL_REQUEST_TEMPLATE.md` and should be used instead of ad hoc naming:

- Branches: `{type}/#{issue-number}-{description}` (e.g. `feat/#1-core-setup`)
- Commits: `{type}: {한글 요약}` (e.g. `feat: BaseEntity 추가`, `refactor: GeneralException 수정`)
- PRs target `develop`

`type` is one of `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `style`.
