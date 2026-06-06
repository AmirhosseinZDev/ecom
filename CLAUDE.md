# Ecommerce Application

## Overview

End-user e-commerce REST API. Users can browse and purchase without registering; account creation
is opt-in at checkout. Authentication is OTP-based (SMS ticket system) plus password.

## Module Structure

Single Maven module (`com.ecommerce:ecommerce`), packaged as the runnable Spring Boot jar.
All code lives under the `com.ecommerce` base package:

```
src/main/java/com/ecommerce/
├── application/   # Spring Boot entry point, REST controllers, services, security, SMS client
└── persistence/   # JPA entities, Spring Data repositories, cache services
```

> The project used to be a two-module Maven build (`ecommerce-application` + `ecommerce-persistence`)
> under the `com.telegram.ecommerce` package. It was flattened into one module and renamed to
> `com.ecommerce`; the `application` / `persistence` split now survives only as package boundaries.

## Tech Stack

| Layer       | Technology                                                   |
|-------------|--------------------------------------------------------------|
| Runtime     | Java 21, Spring Boot 3.5.x                                   |
| Web         | Spring MVC (`/user/**` base path)                            |
| Security    | Spring Security + JWT (jjwt 0.11.5)                          |
| Persistence | Spring Data JPA + Hibernate 6, PostgreSQL                    |
| Migrations  | Flyway (schema owned by Flyway; `ddl-auto=validate`)         |
| Cache       | Redis via Tedisson (`tedisson-spring-boot-starter`)          |
| Validation  | tosan-validation (`@MobileNumber`, `@NationalCode`, `@UUID`) |
| Build       | Maven (single module)                                        |
| ITest infra | Testcontainers (PostgreSQL) + WireMock (SMS), JUnit 5        |

## Database

- Flyway migrations live in `src/main/resources/db/migration/`.
- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate validates entity↔column alignment but
  never creates or alters tables; all DDL is owned by Flyway.
- `spring.flyway.baseline-on-migrate=false` — assumes a fresh database. To apply Flyway on a DB
  already created by the old `ddl-auto=update`, set `baseline-on-migrate=true` and
  `baseline-version=1` before the first startup.

## Key Architectural Decisions

### Mobile number as the primary identifier

- `AppUser.username` stores the mobile number (set during signup for Spring Security compatibility).
- `AppUser.mobile` also stores the mobile number (used for explicit mobile-based lookups).
- `AppUser.isRegistered = true` is required to log in; unregistered or guest users cannot authenticate.
- `UserDetailServiceImpl` checks both `isEnabled` AND `isRegistered` before granting access.

### OTP ticket infrastructure

Every OTP flow owns its own isolated stack — signup and login cannot interfere with each other's
rate-limits or block lists:

```
CacheName enum
  SIGNUP_TICKET / SIGNUP_LAST_TICKET  ←  SignupTicketCacheService
  LOGIN_TICKET  / LOGIN_LAST_TICKET   ←  LoginTicketCacheService
         ↓                  ↓
  AbstractTicketCacheService  (all cache ops live here)

  SignupProperties (signup.ticket.*)  ←  SignupTicketService
  LoginProperties  (login.ticket.*)   ←  LoginTicketService
         ↓                  ↓
  AbstractTicketService  (prepareTicket, sendTicketMessage, validateTicket)
```

**Adding a new OTP flow** (e.g., "admin login"):

1. Add two `CacheName` values: `ADMIN_TICKET`, `ADMIN_LAST_TICKET`.
2. Create `AdminTicketCacheService extends AbstractTicketCacheService`.
3. Create `AdminProperties` with a `TicketProperties ticket` field.
4. Create `AdminTicketService extends AbstractTicketService`.
5. Register `adminProperties()` bean in `EcommercePropertiesConfiguration` (`prefix = "admin"`).
6. Add `admin.ticket.*` config in `application.properties`.

### Signup token cache

After OTP validation the service issues a short-lived UUID "signup token" stored in
`SignupCacheService` (cache `SIGNUP_TOKEN`). The client uses this token in the `/user/signup`
call to prove it completed OTP verification. This separates the OTP validation step from the
registration step.

## Authentication Flows

### Sign-up

```
POST /user/signup-ticket           ← mobile → sends OTP
POST /user/signup-ticket/validation ← mobile + OTP → signupToken
POST /user/signup                  ← signupToken + firstName + lastName + password
```

### Login with password

```
POST /user/check-registration  ← mobile → {isRegistered}  (public)
POST /user/login               ← mobile + password → JWT
```

### Login with OTP

```
POST /user/login-ticket            ← mobile → sends OTP (only if isRegistered)
POST /user/login-ticket/validation ← mobile + OTP → JWT
```

### Change password (authenticated)

```
POST /user/change-password  ← currentPassword + newPassword + confirmPassword
```

No OTP is required. The endpoint is protected by JWT.

## Security Configuration

Public endpoints are declared in `SecurityConfiguration.PUBLIC_ENDPOINTS[]`.
Everything else requires a valid JWT in the `Authorization: Bearer <token>` header.

JWT expiry: `security.jwt.expiration-time` (default 1 h).
JWT secret: `security.jwt.secret-key` (Base64-encoded HMAC-SHA256 key ≥ 256 bits).

## Testing Conventions

Two independent test tiers live side by side under `src/test/java`:

### Unit tests (`*UTest`) — surefire

| Convention           | Rule                                                                             |
|----------------------|----------------------------------------------------------------------------------|
| Test class name      | `<Subject>_<methodUnderTest>UTest.java`                                          |
| Test method name     | `snake_case` describing the scenario                                             |
| Framework            | JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)                        |
| Service construction | Always construct manually in `@BeforeEach`; never use `@InjectMocks` on services |
| Cache service tests  | Use `@InjectMocks` on concrete cache classes (constructor injection via Mockito) |
| Scope                | Unit tests only; no Spring context is loaded                                     |

Unit tests are **skipped by default** and enabled by the `release` profile:

```
mvn test -P release
```

### Integration tests (`*ITest`) — failsafe

Live in `com.ecommerce.application.integration`. They boot the **full** Spring context once
(`@SpringBootTest` + `@ActiveProfiles("test")`) and drive the app through the real HTTP stack with
`MockMvc` — security filters, `ValidationAspect`, controllers, services, JPA.

- **PostgreSQL** runs in a Testcontainers container via the JDBC-URL driver
  (`jdbc:tc:postgresql:15-alpine:///ecom?TC_DAEMON=true`); Flyway applies the real migrations to it.
- **The SMS provider is mocked** with an in-process WireMock server on a fixed port (`9576`); tests
  read the OTP back out of the captured SMS request body to complete each flow.
- Redis stays disabled (Caffeine cache). The shared context + container are reused for the whole
  suite (`reuseForks=true`, single fork); each test truncates `app_user` and resets WireMock.
- Overrides live in `src/test/resources/config/application-test.properties`.
- `AbstractIntegrationITest` holds the shared harness (WireMock stubs, OTP capture, HTTP + signup
  helpers). Scenario classes: `SignupFlowITest`, `LoginTicketFlowITest`, `ChangePasswordFlowITest`.

Integration tests run on `verify` (they need Docker running):

```
mvn verify          # runs *ITest via failsafe
mvn verify -P release   # *UTest (surefire) + *ITest (failsafe)
```

## Development Setup

### Prerequisites

- Java 21
- PostgreSQL 14+ running at `localhost:5432`
    - database: `mydb`, user: `ecom`, password: `ECOM`
- Redis (optional) — set `tedisson.redis.enabled=true` for Redis-backed cache;
  defaults to in-memory Caffeine when disabled

### First run

```bash
# Flyway will apply V1__initial_schema.sql automatically on startup
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Properties files

All config files live under `src/main/resources/config/` (`src/test/resources/config/` for tests).

| File                          | Purpose                                            |
|-------------------------------|----------------------------------------------------|
| `application.properties`      | Base config (committed, no secrets)                |
| `application-dev.properties`  | Local `dev` overrides (gitignored)                 |
| `application-test.properties` | `test` overrides: Testcontainers DB + WireMock SMS |

## Common Gotchas

- **`TicketCacheService` was split in May 2026.** The old single class is gone; use
  `SignupTicketCacheService` or `LoginTicketCacheService` depending on the flow.
- **`USER_LAST_TICKET` was renamed to `SIGNUP_LAST_TICKET`** in the same refactor.
  Any Redis key with the old cache name is stale after a cache flush.
- **`AppUser.name` was split into `firstName` + `lastName`** in the auth refactor.
  There is no `name` column in the DB.
- **`AppUser.username` = mobile number.** National ID is no longer used as the username.
  It lives in the optional `national_id` column.
- **`changePassword` does not re-verify the current password.** `ChangePasswordRequestDto` only
  carries `newPassword` + `confirmPassword`; the service just checks they match and re-hashes.
- **`AppUser.mobile` is a real column.** It is set (alongside `username`) during signup and is what
  every `findByMobile` lookup keys on, so the OTP/login flows break if it is left null. `MOBILE` is a
  nullable, unique `VARCHAR(20)` column in `V1__initial_schema.sql`.
- **Flyway `baseline-on-migrate=false`** — do not set this to `true` in production without
  also setting the correct `baseline-version` to match the already-applied schema.
- **`com.telegram.ecommerce` → `com.ecommerce`.** The old package name is gone; the build is now a
  single Maven module (no `-pl` flags). Stale imports referencing `com.telegram` will not resolve.
