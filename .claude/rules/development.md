# Development Rules

Non-obvious conventions and gotchas. Anything derivable from reading the code is not repeated here.
For architecture see [CLAUDE.md](../../CLAUDE.md); for business logic see [business.md](business.md).

---

## Running Locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn verify              # integration tests (needs Docker)
mvn verify -P release   # unit + integration tests
```

---

## Testing Conventions

Unit tests (`*UTest`): JUnit 5 + Mockito. Construct services manually in `@BeforeEach` — never `@InjectMocks` on services (`@InjectMocks` is fine on cache classes). No Spring context.

Integration tests (`*ITest`): full `@SpringBootTest` + `@ActiveProfiles("test")`. Testcontainers PostgreSQL, WireMock SMS on port `9576`, Caffeine cache (Redis disabled). Context reused across suite; each test truncates `app_user` and resets WireMock.

---

## Coding Conventions

- **No comments** unless the *why* is non-obvious (hidden constraint, workaround, invariant).
- **Enums over string constants.** Never use raw `String` for a fixed set of values. Spring MVC converts `@RequestParam` / `@PathVariable` enums automatically by name.
- **DTO-layer enums** go in `application/api/dto/<domain>/enumeration/`. Persistence enums go in `persistence/entity/enumeration/`.
- **Search/filter endpoints always use a DTO.** Bind with `@ModelAttribute *SearchRequestDto` in the controller; pass the DTO directly to the service. Never individual `@RequestParam` args for filter criteria.
- **Error handling** — never serialize a raw `Throwable`. Populate `ExceptionParam` from `EcommerceException.getData()` only.
- **Cache TTL** — always pass a meaningful `Duration` to `AppCacheManager.put()`. `null` or zero falls back to the global default, which is wrong for OTP/block entries.

---

## MapStruct

- **Annotation processor order is mandatory**: `lombok` → `lombok-mapstruct-binding` → `mapstruct-processor` → `hibernate-jpamodelgen`. Wrong order breaks generated code.
- **`apply(RequestDto, @MappingTarget Entity)`**: always ignore server-managed fields — `id`, `code`, `createdAt`, `updatedAt`, `mainImage`, `otherImages`.
- **`@ElementCollection` (e.g., `prices`)**: MapStruct generates `clear()` + `addAll()` on `@MappingTarget`, which preserves Hibernate's `PersistentBag` identity. Do not replace with manual mapping.
- **Child entity lists**: add a `toXxxDto(ChildEntity)` method to the mapper so MapStruct auto-maps `List<ChildEntity>` inside `toResponseDto`.

---

## Common Gotchas

- **Redisson is not auto-configured.** Both `RedissonAutoConfigurationV2` and `V4` are excluded. Redis beans only exist when `app.cache.type=redis`.
- **`@Enumerated(EnumType.STRING)`** is required on enum fields inside `@Embeddable` used in `@ElementCollection`. Without it, Hibernate stores the ordinal.
- **`@JdbcTypeCode(SqlTypes.JSON)`** is required for `Map<SpecificationKey, String> specification` (JSONB). `ObjectMapper` has `ACCEPT_CASE_INSENSITIVE_ENUMS` — enum keys round-trip correctly.
- **Images are base64, not files.** No file-storage service, no upload directory. `MultipartFile.getBytes()` is base64-encoded and stored directly in the DB column.
- **`ProductOtherImage` uses `orphanRemoval = true`.** Removing from `product.getOtherImages()` and saving the parent deletes the row — no need to call the repository directly.
- **Flyway `baseline-on-migrate=false`** — do not set `true` in production without a matching `baseline-version`. Migrations use `V1`, `V1.1`, `V1.2` … sub-version scheme; never edit an existing file, always add a new one (`V1.6`, `V1.7`, …).
- **Config files are `.yml`, not `.properties`.** The `FileSystemResource("config/application.properties")` path in `EcommerceConfiguration` is a dead path — non-fatal since Boot resolves via its own `Environment`.
