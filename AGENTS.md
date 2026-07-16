# Repository Guidelines

## Project Structure & Module Organization

KOMME-BE is a Java 21, Spring Boot 4 REST API built with Gradle. Production code lives under `src/main/java/com/komme`; shared infrastructure is in `com.komme.common`. Tests mirror packages under `src/test/java`. Runtime configuration belongs in `src/main/resources/application.yaml`.

Add features under `course`, `spot`, `tourapi`, `i18n`, or `auth`. Consult `.claude/rules/<domain>/rule.md` before changing a domain. Keep cross-cutting code in `common`.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper.

- `./gradlew build` — compile the project and run all tests.
- `./gradlew test` — run the JUnit test suite.
- `./gradlew test --tests "com.komme.KommeBeTempApplicationTests"` — run one test class.
- `./gradlew bootRun` — start the API locally.

The context test requires a configured test DataSource. Supply test database settings before treating failures as regressions.

## Coding Style & Naming Conventions

Use four-space indentation, one public type per file, PascalCase classes, camelCase members, and lowercase packages. Use Lombok consistently. No formatter or linter is configured, so match nearby code.

JPA entities should extend `BaseEntity`. Controllers should return `ResponseEntity<ApiResponse<T>>` through `ApiResponse` factory methods. Represent domain failures with `GeneralException` and a domain-specific enum implementing `BaseStatus`; avoid ad hoc response bodies and exception types.

## Testing Guidelines

Tests use JUnit 5. Mirror production packages, name classes `*Tests`, and use descriptive camelCase methods. Add focused tests for new behavior and integration tests where Spring wiring matters. Run `./gradlew test` before opening a pull request.

## Commit & Pull Request Guidelines

Use `{type}: {Korean summary}`, for example `feat: 코스 조회 API 추가`. Common types are `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, and `style`. Branches follow `{type}/#{issue-number}-{description}` and PRs target `develop`.

PRs must reference the resolved issue, summarize what and why, identify review concerns, and confirm relevant tests. Keep commits logically scoped; do not mix unrelated configuration and feature changes.

## Agent Workflow

Do not modify code when a user only describes a feature or asks for advice. First explain its scope, product direction, implementation approach, architectural impact, alternatives, trade-offs, edge cases, and testing needs so the user can choose a direction.

Change files only after an explicit instruction such as “작성해줘,” “만들어줘,” “구현해줘,” “수정해줘,” or “적용해줘.” “분석해줘,” “어떻게 하면 좋을까?”, and “방향을 알려줘” authorize inspection and analysis only. After approval, apply the agreed direction and verify it.

## Security & Configuration

Never commit secrets to `application.yaml`. Use environment variables or ignored configuration and safe test credentials.
