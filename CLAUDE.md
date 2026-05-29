# Ecommerce Application

## Overview

End-user e-commerce REST API. Users can browse and purchase without registering; account creation
is opt-in at checkout. Authentication is OTP-based (SMS ticket system) plus password.

## Module Structure

```
ecommerce/
├── ecommerce-application/   # Spring Boot entry point, REST controllers, services, security
└── ecommerce-persistence/   # JPA entities, Spring Data repositories, cache services
```

`ecommerce-persistence` is a plain library; `ecommerce-application` is the runnable jar.

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
| Build       | Maven multi-module                                           |

## Database

- Flyway migrations live in `ecommerce-application/src/main/resources/db/migration/`.
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

| Convention           | Rule                                                                             |
|----------------------|----------------------------------------------------------------------------------|
| Test class name      | `<Subject>_<methodUnderTest>UTest.java`                                          |
| Test method name     | `snake_case` describing the scenario                                             |
| Framework            | JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)                        |
| Service construction | Always construct manually in `@BeforeEach`; never use `@InjectMocks` on services |
| Cache service tests  | Use `@InjectMocks` on concrete cache classes (constructor injection via Mockito) |
| Scope                | Unit tests only; no Spring context is loaded                                     |

Run only unit tests (skipped by default, enabled in `release` Maven profile):

```
mvn test -P release -pl ecommerce-application,ecommerce-persistence
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
mvn spring-boot:run -pl ecommerce-application -Dspring-boot.run.profiles=dev
```

### Properties files

| File                         | Purpose                             |
|------------------------------|-------------------------------------|
| `application.properties`     | Base config (committed, no secrets) |
| `application-dev.properties` | Local overrides (gitignored)        |

## Common Gotchas

- **`TicketCacheService` was split in May 2026.** The old single class is gone; use
  `SignupTicketCacheService` or `LoginTicketCacheService` depending on the flow.
- **`USER_LAST_TICKET` was renamed to `SIGNUP_LAST_TICKET`** in the same refactor.
  Any Redis key with the old cache name is stale after a cache flush.
- **`AppUser.name` was split into `firstName` + `lastName`** in the auth refactor.
  There is no `name` column in the DB.
- **`AppUser.username` = mobile number.** National ID is no longer used as the username.
  It lives in the optional `national_id` column.
- **`changePassword` does not re-verify the current password** in the service layer — the DTO
  field `currentPassword` exists for future use but is currently not checked.
- **Flyway `baseline-on-migrate=false`** — do not set this to `true` in production without
  also setting the correct `baseline-version` to match the already-applied schema.
