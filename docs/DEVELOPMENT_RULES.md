# Development Rules

Conventions, setup, and gotchas for working on the Ecommerce application.
For architecture see [CLAUDE.md](../CLAUDE.md); for business logic see [BUSINESS_RULES.md](BUSINESS_RULES.md).

---

## Prerequisites

- JDK 25 (CI toolchain must also be 25 — the app uses relaxed `main` rules introduced in JDK 21+)
- PostgreSQL 14+ at `localhost:5432`
    - database: `mydb`, user: `ecom`, password: `ECOM`
- Redis at `localhost:6379` — **only required** when `app.cache.type=redis`; the default is Caffeine (no Redis needed)
- Docker — required to run integration tests (Testcontainers spins up PostgreSQL)

---

## Running Locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Flyway applies migrations automatically on first startup.

---

## Config File Layout

| File                                                    | Purpose                                           |
|---------------------------------------------------------|---------------------------------------------------|
| `src/main/resources/config/application.yml`             | Base config (committed; no prod secrets)          |
| `src/main/resources/config/application-dev.yml`         | Local `dev` overrides                             |
| `src/test/resources/config/application-test.yml` | Test overrides: Testcontainers DB + WireMock SMS  |

All config files are `.yml`. The `test` profile override is loaded by Spring Boot automatically
(`classpath:/config/application-test.yml`) — there is no separate `EcommercePlaceHolderConfigurer`
bean for the test profile.

---

## Testing

Two independent tiers, both under `src/test/java`:

### Unit tests (`*UTest`) — surefire

| Convention           | Rule                                                               |
|----------------------|--------------------------------------------------------------------|
| Class name           | `<Subject>_<methodUnderTest>UTest.java`                            |
| Method name          | `snake_case` describing the scenario                               |
| Framework            | JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)          |
| Service construction | Construct manually in `@BeforeEach`; **never** `@InjectMocks` on services |
| Cache service tests  | `@InjectMocks` on concrete cache classes is fine (ctor injection)  |
| Spring context       | Not loaded — pure unit scope only                                  |

Skipped by default; enabled by the `release` profile:

```bash
mvn test -P release
```

### Integration tests (`*ITest`) — failsafe

Boot the **full** Spring context once (`@SpringBootTest` + `@ActiveProfiles("test")`) and drive
the app through the real HTTP stack with `MockMvc` — security filters, `ValidationAspect`,
controllers, services, JPA.

- **PostgreSQL** via Testcontainers JDBC URL (`jdbc:tc:postgresql:15-alpine:///ecom?TC_DAEMON=true`);
  Flyway applies real migrations once.
- **SMS provider** mocked by an in-process WireMock server on fixed port `9576`; tests read the
  OTP back from the captured request body.
- **Redis is disabled** — `app.cache.type=caffeine` in test properties; no Redis needed.
- Context and container are reused for the whole suite (`reuseForks=true`, single fork). Each test
  truncates `app_user` and resets WireMock.
- Base harness: `AbstractIntegrationITest`. Scenario classes: `SignupFlowITest`,
  `LoginTicketFlowITest`, `ChangePasswordFlowITest`.

```bash
mvn verify              # *ITest via failsafe (needs Docker)
mvn verify -P release   # *UTest (surefire) + *ITest (failsafe)
```

### What the test suite does NOT cover (known gaps)

- Caffeine TTL expiry — no test waits out a TTL boundary; per-entry expiry is exercised only
  through explicit `evict` calls.
- Localized error messages — no test asserts the resolved `message` string from `messages*.properties`.
- Context startup with Redisson and no Redis — integration suite always uses Caffeine.

---

## Coding Conventions

- **No comments by default.** Add one only when the *why* is non-obvious (hidden constraint,
  workaround, subtle invariant). Never describe what the code does.
- **Error handling** — `ExceptionHandlerUtil` must never serialize a raw `Throwable`. Populate
  `ExceptionParam` fields directly from `EcommerceException.getData()`.
- **Cache TTL** — always pass a meaningful `Duration` to `AppCacheManager.put()`. Passing `null`
  or zero falls back to the global Caffeine default (`app.cache.caffeine.ttl`), which is wrong for
  OTP/block entries with specific expiry semantics.

---

## Common Gotchas

- **Redisson is not auto-configured.** `RedissonAutoConfigurationV2` (Spring Boot 2.7+) and
  `RedissonAutoConfigurationV4` (Spring Boot 4+) are both excluded in `application.yml`.
  Redis-related beans (`RedisCacheManagerImpl`, `RedissonClient`) are only present when
  `app.cache.type=redis`. The app starts with no Redis by default.
- **Flyway `baseline-on-migrate=false`** — do not set `true` in production without a matching
  `baseline-version`.
- **Config files are `.yml`, not `.properties`.** References to `config/application.properties` in
  `EcommerceConfiguration.getResources()` are a dead path (the file does not exist); the else-branch
  is misleading but non-fatal since Boot resolves properties via its own `Environment`.
