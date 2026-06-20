# Business Rules

Non-obvious domain decisions. Anything obvious from reading the code is not repeated here.
For architecture see [CLAUDE.md](../../CLAUDE.md).

---

## User Model

- Mobile number is stored in **both** `AppUser.username` (Spring Security) and `AppUser.mobile` (explicit lookups).
- `UserDetailServiceImpl` checks **both** `isEnabled` AND `isRegistered`. A user with `isRegistered=false` cannot log in even if their account exists.
- `firstName` + `lastName` — there is no `name` column.

---

## Authentication Flows

```
POST /user/signup-ticket            ← mobileNumber
POST /user/signup-ticket/validation ← mobileNumber + OTP  →  signupToken (UUID, short-lived)
POST /user/signup                   ← signupToken + firstName + lastName + password

POST /user/check-registration       ← mobileNumber  →  { isRegistered: true|false }

POST /user/login                    ← mobileNumber + password  →  JWT

POST /user/login-ticket             ← mobileNumber  (rejected if not isRegistered)
POST /user/login-ticket/validation  ← mobileNumber + OTP  →  JWT

POST /user/change-password          ← newPassword + confirmPassword  (JWT required)
```

`signupToken` is stored in `SignupCacheService` (cache `SIGNUP_TOKEN`) and proves OTP was completed.

`changePassword` does **not** verify the current password — `ChangePasswordRequestDto` has only `newPassword` + `confirmPassword`.

---

## OTP Rules

Signup and login flows are **completely isolated** — separate rate-limit counters, block lists, and cache buckets.

| Property       | Config key                            | Default |
|----------------|---------------------------------------|---------|
| Length         | `app.{flow}.ticket.length`            | 6       |
| TTL            | `app.{flow}.ticket.time-to-live`      | 2 min   |
| Block duration | `app.{flow}.ticket.block-duration`    | 10 min  |
| Max failures   | `app.{flow}.ticket.max-failure-count` | 5       |

After `max-failure-count` failures, the mobile is blocked for `block-duration` (`TICKET_BLOCKED`). Re-send within TTL is rejected with `SEND_TICKET_TIME_LIMIT`.

### OTP class map

```
CacheName: SIGNUP_TICKET / SIGNUP_LAST_TICKET  ←  SignupTicketCacheService
CacheName: LOGIN_TICKET  / LOGIN_LAST_TICKET   ←  LoginTicketCacheService
                    ↓
         AbstractTicketCacheService

SignupProperties / LoginProperties  ←  SignupTicketService / LoginTicketService
                    ↓
         AbstractTicketService (prepareTicket, sendTicketMessage, validateTicket)
```

To add a new OTP flow: add two `CacheName` values, extend `AbstractTicketCacheService` and `AbstractTicketService`, create a `*Properties` bean in `EcommercePropertiesConfiguration`, add config in `application.yml`.

---

## Product Catalog — Non-Obvious Rules

- **`code` is server-generated**, never from the client: `{categoryId}-{nextval('product_code_seq')}`. `ProductMapper.apply()` ignores it; set it after mapping in `ProductService.create()`.
- **Images are base64 stored in PostgreSQL**, not files. `mainImage` is embedded in the `product` row (`main_image_data TEXT`). `otherImages` are rows in `product_other_image`, each with its own `id`.
- **`ProductOtherImage` removal uses `imageId`**, not a list index. `DELETE /products/{id}/images?type=OTHER&imageId={imageId}`.
- **`isAvailable` filter** checks `inventoryCount` directly (`> 0` = available), not `inventoryStatus`.
- **`USER_LAST_TICKET` is gone** — renamed `SIGNUP_LAST_TICKET`. Stale cache keys with the old name are invalid after a flush.

---

## Shopping Cart — Non-Obvious Rules

```
GET    /cart                            ← current user's cart (created on first access)
POST   /cart/items                      ← productId + variantType + quantity
PATCH  /cart/items/{itemId}             ← quantity  (absolute set)
POST   /cart/items/{itemId}/increment   ← +1
POST   /cart/items/{itemId}/decrement   ← -1  (line removed when it would hit 0)
DELETE /cart/items/{itemId}             ← remove one line
DELETE /cart                            ← clear all lines
```

- All cart endpoints require a JWT (any role); they are **not** in `PUBLIC_ENDPOINTS`. The acting user is taken from the JWT principal (`UserDetailsDto.getId()`), never from the request body — a user can only touch their own cart.
- **One cart per user** (`uk_cart_user`). `GET /cart` and `DELETE /cart` lazily create the cart (cart creation is race-safe — concurrent first-access falls back to a re-read); mutating a specific item (`PATCH`/increment/decrement/`DELETE /cart/items/{id}`) when no cart exists returns `CART_ITEM_NOT_FOUND`. Clearing a non-existent cart is a no-op that returns an empty cart.
- **A line is keyed by `(cart, productId, variantType)`** (`uk_cart_item_product_variant`). The same product in a different variant is a separate line. Re-adding the same product+variant **merges** into the existing line (quantities sum).
- **`variantType` must be one the product actually offers** — it must match a `Price.variantType` on the product, else `PRODUCT_VARIANT_NOT_FOUND`.
- **Stock is validated against `Product.inventoryCount`** (single product-level count; the catalog has no per-variant inventory). Add / increment / set-quantity that would push a line above `inventoryCount` returns `INSUFFICIENT_STOCK`. Only `ACTIVE` products are purchasable, else `PRODUCT_NOT_AVAILABLE`.
- **Price is snapshotted at add time** (`cart_item.unit_price` / `discount_price`) so the cart total is stable even if the catalog price later changes. `effectivePrice` / `lineTotal` / `totalPrice` in the response are derived; `discountPrice` wins over `unitPrice` when present.

---

## Error Response Contract

```json
{ "errorCode": "INVALID_TICKET", "message": "...", "errorParams": {} }
```

- `message` resolved from `messages.properties` / `messages_fa.properties` via `Accept-Language`. Falls back to the key.
- `errorParams` is `{ "fieldName": ["msg"] }` for `VALIDATION_ERROR`; `null` for others.
- Stack traces are **never** included.
