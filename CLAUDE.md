# Ecommerce Application

End-user e-commerce REST API — OTP-based authentication (SMS ticket system) plus password.

| Reference               | Link                                              |
|-------------------------|---------------------------------------------------|
| Business rules & flows  | [docs/BUSINESS_RULES.md](docs/BUSINESS_RULES.md) |
| Development rules       | [docs/DEVELOPMENT_RULES.md](docs/DEVELOPMENT_RULES.md) |

---

## Module Structure

Single Maven module (`com.ecommerce:ecommerce`), packaged as the runnable Spring Boot jar.

```
src/main/java/com/ecommerce/
├── application/   # entry point, REST controllers, services, security, SMS client, config
└── persistence/   # JPA entities, Spring Data repositories, cache services
```

> Previously a two-module Maven build (`ecommerce-application` + `ecommerce-persistence`) under
> `com.telegram.ecommerce`. Flattened to one module; the `application`/`persistence` split now lives
> only as a package boundary.

---

## Tech Stack

| Layer        | Technology                                                                |
|--------------|---------------------------------------------------------------------------|
| Runtime      | Java 25 (uses relaxed `main` rules from JDK 21+; CI toolchain must be 25) |
| Web          | Spring Boot 4.1.0 — Spring MVC (`/user/**` base path)                    |
| Security     | Spring Security + JWT (jjwt 0.13.0)                                      |
| Persistence  | Spring Data JPA + Hibernate 7.4.1.Final, PostgreSQL                      |
| Migrations   | Flyway (`spring.flyway.baseline-on-migrate=false` — assumes a fresh DB)   |
| Cache        | Caffeine (default, in-memory) or Redis via Redisson 4.4.0 (opt-in)       |
| Observability| Spring Boot Actuator                                                      |
| Build        | Maven (single module)                                                     |
| ITest infra  | Testcontainers (PostgreSQL) + WireMock (SMS provider), JUnit 5            |

---

## Cache

Two implementations of `AppCacheManager` wired via `@ConditionalOnProperty`:

| `app.cache.type`     | Active bean                | Notes                                 |
|----------------------|----------------------------|---------------------------------------|
| `caffeine` (default) | `CaffeineCacheManagerImpl` | In-memory, per-entry TTL via `Expiry` |
| `redis`              | `RedisCacheManagerImpl`    | Redisson `RBucket` with per-entry TTL |

`RedissonAutoConfigurationV2` is excluded in `application.yml`. `RedissonClient` is created by
`RedissonConfig` only when `app.cache.type=redis` — the app and test suite boot cleanly with no
Redis present.

Caffeine uses `Caffeine.expireAfter(Expiry)` backed by an `ExpiringValue` wrapper (`Instant expiresAt`
per entry). `replace` preserves the original expiry; `put`/`getAndPut` set a new one.

---

## Error Handling & i18n

`ExceptionHandlerUtil` builds every error response from `EcommerceException`:

- **Only `errorCode`, `message`, `errorParams`** are included — no stack traces or internal state.
- **`message`** is resolved via `MessageSource` + `LocaleContextHolder.getLocale()`. Bundles:
  `messages.properties` (English), `messages_fa.properties` (Farsi). Falls back to the raw key.
- **`errorParams`** for `VALIDATION_ERROR` is `{ "fieldName": ["msg", ...] }` built as
  `Map<String, Object>` by `ValidationAspect` — not re-serialized through Jackson.

---

## Database

- Flyway migrations: `src/main/resources/db/migration/`
- `spring.jpa.open-in-view=false`
- `baseline-on-migrate=false` — fresh DB assumed. See [DEVELOPMENT_RULES.md](docs/DEVELOPMENT_RULES.md)
  for the migration path from a legacy `ddl-auto=update` database.

---

## Security

Public endpoints declared in `SecurityConfiguration.PUBLIC_ENDPOINTS[]`.
Everything else requires `Authorization: Bearer <JWT>`.

- JWT expiry: `security.jwt.expiration-time` (default `1h`)
- JWT secret: `security.jwt.secret-key` (Base64 HMAC-SHA256, ≥ 256 bits) — **committed; rotate before prod.**
