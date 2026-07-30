# Repository Guidelines

## Project Structure & Module Organization

KOMME-BE is a Java 21, Spring Boot 4 REST API built with Gradle. Production code lives under `src/main/java/com/komme`; shared infrastructure is in `com.komme.common`. Tests mirror packages under `src/test/java`. Runtime configuration belongs in `src/main/resources/application.yaml`.

Add features under the owning domain package such as `course`, `spot`, `tourapi`, `i18n`, `auth`, or `user`. Consult `.claude/rules/<domain>/rule.md` before changing a domain. Keep cross-cutting code in `common`.

Choose packages by domain ownership and responsibility, not by the first caller that needs the code. Before adding a class, decide which domain owns the concept and place it under that domain's package. For example, user profile APIs and user-specific errors belong under `domain.user`, even if they are used by authentication flows. Authentication-only concerns such as JWT, OAuth login, password login, and token lifecycle belong under `domain.auth`.

Within each domain, keep classes grouped by responsibility (`entity`, `enums`, `repository`, `dto/request`, `dto/response`, `service`, `controller`, `controller/docs`, `exception`, and domain-specific infrastructure packages such as `jwt` or `properties`). Put exception statuses and exception mappers in the owning domain's `exception` package, not in `service`. Avoid placing a class in another domain just because it is convenient to inject from there.

For authentication-related changes, keep the package layout grouped by responsibility (`entity`, `enums`, `repository`, `dto/request`, `dto/response`, `service`, `controller/docs`, `jwt`, and `properties`). Keep domain enums in the owning domain's `enums` package.

For email verification, scope Redis keys by purpose such as `SIGN_UP` or `PASSWORD_RESET`. Sign-up may use an email verified flag, but password reset must issue a short-lived one-time reset token mapped to the email and consume that token when changing the password. Password reset is allowed only for registered `LOCAL` accounts and must invalidate all refresh tokens after success.

Keep the password format rule (regex and error message) in a single `domain.auth.util.PasswordPolicy` constant. Every request DTO that accepts a new password (sign-up, password change, password reset) must reference `PasswordPolicy.PATTERN`/`PasswordPolicy.MESSAGE` instead of declaring its own `@Pattern`, so the policy cannot drift between endpoints.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper.

- `./gradlew build` — compile the project and run all tests.
- `./gradlew test` — run the JUnit test suite.
- `./gradlew test --tests "com.komme.KommeBeTempApplicationTests"` — run one test class.
- `./gradlew bootRun` — start the API locally.

The context test requires a configured test DataSource. Supply test database settings before treating failures as regressions.

## Coding Style & Naming Conventions

Use four-space indentation, one public type per file, PascalCase classes, camelCase members, and lowercase packages. Use Lombok consistently. No formatter or linter is configured, so match nearby code.

Use Lombok `@RequiredArgsConstructor` for constructor-based dependency injection. Do not hand-write service/controller constructors or use field injection. Avoid fully qualified class names in method bodies; import project classes using consistent package ordering and keep imports readable.

Place a one-line Korean comment immediately above every written method. Comments should describe the method as a concise noun phrase ending in terms such as `~기능`, `~생성`, or `~검증`; do not use sentence-style endings.

Keep methods short and give repeated or independently meaningful logic a private/helper method or a dedicated component. Prefer reusable domain factories and behavior methods over duplicating entity construction in services.

For entities, use `BaseEntity`, the standard JPA entity annotations, a protected no-args constructor, and a private builder constructor when a builder is useful. Expose creation through named static factory methods (for example, `createLocal` or `createOAuth`); services should call those factories instead of invoking builders directly.

Separate request and response DTOs into `dto/request` and `dto/response`. Name request and response DTOs with the action verb first, such as `ChangeNicknameRequest` instead of `NicknameChangeRequest`. Controllers should expose documentation through a `controller/docs` interface and implement that interface in the concrete controller. Document request fields, success responses, and domain-specific error responses in Swagger.

Keep status enum entries ordered by HTTP response code in ascending order (for example, 400 before 401, then 409 and 500).

JPA entities should extend `BaseEntity`. Controllers should return `ResponseEntity<ApiResponse<T>>` through `ApiResponse` factory methods. Represent domain failures with `GeneralException` and a domain-specific enum implementing `BaseStatus`; avoid ad hoc response bodies and exception types. Keep error statuses in the owning domain's exception package (for example, user API failures should use `UserErrorStatus`, not `AuthErrorStatus`).

## Testing Guidelines

Tests use JUnit 5. Mirror production packages, name classes `*Tests`, and use descriptive camelCase methods. Add focused tests for new behavior and integration tests where Spring wiring matters. Run `./gradlew test` before opening a pull request.

## Commit & Pull Request Guidelines

Use `{type}: {Korean summary}`, for example `feat: 코스 조회 API 추가`. Common types are `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, and `style`. Branches follow `{type}/#{issue-number}-{description}` and PRs target `develop`.

PRs must reference the resolved issue, summarize what and why, identify review concerns, and confirm relevant tests. Keep commits logically scoped; do not mix unrelated configuration and feature changes.

## Agent Workflow

Do not modify code when a user only describes a feature or asks for advice. First explain its scope, product direction, implementation approach, architectural impact, alternatives, trade-offs, edge cases, and testing needs so the user can choose a direction.

Change files only after an explicit instruction such as “작성해줘,” “만들어줘,” “구현해줘,” “수정해줘,” or “적용해줘.” “분석해줘,” “어떻게 하면 좋을까?”, and “방향을 알려줘” authorize inspection and analysis only. After approval, apply the agreed direction and verify it.

## Security & Configuration

Keep shared non-secret defaults in `application.yaml`. Put local development values in the ignored `application-local.yaml`, and keep production configuration in `application-prod.yaml` using environment-variable placeholders such as `${MAIL_HOST}`. Never commit secrets to tracked configuration; use ignored local configuration, environment variables, or safe test credentials.
