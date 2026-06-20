---
name: review
description: Code review checklist for this ecommerce project. Checks for dead error types, orphaned entities, message bundle drift, transaction strategy, and silent no-ops. Run before opening a PR.
user-invocable: true
allowed-tools:
  - Read
  - Bash
---

# /review — PR Review Checklist

Run each check below against the current branch. Report findings grouped by section. For each finding state: file, line (if applicable), what is wrong, and what the fix is.

---

## 1. Dead error types

Scan `src/main/java/com/ecommerce/application/api/exception/ECOMErrorType.java` for every enum constant.
For each constant, grep the entire `src/` tree (excluding the enum file itself) for its name.
Flag any constant that has zero references — it is never thrown.

**Why:** `code` is server-generated from a sequence (no collision possible).

**Fix:** Delete the constant and its corresponding message keys from both `messages.properties` and `messages_fa.properties`.

---

## 2. Orphaned entities and repositories

For every class in `src/main/java/com/ecommerce/persistence/entity/` and `src/main/java/com/ecommerce/persistence/repository/`, grep `src/` (excluding the file itself) for the class name.
Flag any that have zero references outside their own file.

Also check whether the corresponding DB tables/sequences are still referenced in any migration file under `src/main/resources/db/migration/`. If the entity is gone but the tables remain, a new Flyway migration is needed to drop them (and any FKs that point to them from other tables).

**Why:** Orphaned entities imply a subsystem (e.g., media) that no longer exists. They cause confusion and Hibernate will still validate/create the schema for them.

**Fix:** Delete the entity and repository. Remove `@Column` fields on other entities that hold a bare `Long` FK to the deleted table. Write a new `V1.x__drop_*.sql` migration (using `DROP ... IF EXISTS`) to remove the tables, FKs, and sequences.

---

## 3. Message bundle drift

Every key in `messages.properties` and `messages_fa.properties` must correspond to a `messageKey` value in `ECOMErrorType`.
Every `messageKey` in `ECOMErrorType` must have an entry in both bundle files.

Check for:
- Keys in the bundle that no longer exist in `ECOMErrorType` (stale keys).
- `messageKey` values in `ECOMErrorType` that are missing from a bundle file (missing translations).

**Why:** Stale keys are left-over from deleted error types and mislead maintainers. Missing keys cause the error message to fall back to the raw key string at runtime.

**Fix:** Delete stale keys. Add missing keys with an appropriate translation.

---

## 4. Transaction strategy on service methods

Check every `@Service` class under `src/main/java/com/ecommerce/application/service/`.

Flag:
- Class-level `@Transactional` without `readOnly = true` (too broad — write transaction opened for all methods including reads).
- Read methods (those that only call `findById`, `findAll`, or query methods and pass the result to a mapper) that have no `@Transactional` at all (risk of `LazyInitializationException` when MapStruct accesses lazy collections).
- Read methods annotated with plain `@Transactional` instead of `@Transactional(readOnly = true)` (missed optimisation — Hibernate dirty-checks all loaded entities unnecessarily).

**Expected pattern:**
```
@Transactional               → create / update / uploadImage / removeImage / delete
@Transactional(readOnly=true)→ getById / search / any pure-read method
// no class-level @Transactional
```

**Why:** `readOnly = true` does NOT disable lazy loading — `@Transactional` keeps the Hibernate session open regardless of the flag. `readOnly = true` tells Hibernate to skip dirty checking at flush time, which is free optimisation on read paths. Class-level `@Transactional` (without readOnly) opens a write-capable transaction for every method, including GET endpoints, which is wasteful.

---

## 5. Silent no-ops on required conditional parameters

Check every controller endpoint where a `@RequestParam(required = false)` parameter is required for a specific value of another parameter.

Example pattern to look for:
```java
void removeImage(Long productId, ImageType type, Long imageId)
// imageId is required when type == OTHER, but declared required=false
```

Flag any method where a null value for the optional parameter would cause the operation to silently succeed (return 2xx) while doing nothing.

**Why:** A caller omitting `imageId` when `type=OTHER` gets a 200 OK but nothing is deleted — the bug is invisible. The contract must be enforced explicitly.

**Fix:** Add a guard at the top of the service method:
```java
if (type == ImageType.OTHER && imageId == null) {
    throw new EcommerceException(ECOMErrorType.VALIDATION_ERROR);
}
```
And add an integration test that calls the endpoint with the missing parameter and asserts 400 with `errorCode = VALIDATION_ERROR`.
