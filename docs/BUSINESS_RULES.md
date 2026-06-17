# Business Rules

Business logic, domain rules, and product-level flows for the Ecommerce application.
For technical setup see [CLAUDE.md](../CLAUDE.md).

---

## User Model

- **Mobile number is the primary identifier.** It is stored in both `AppUser.username` (for Spring
  Security compatibility) and `AppUser.mobile` (for explicit lookups). National ID is no longer the
  username; it lives in the optional `national_id` column.
- `AppUser.isRegistered = true` is required to log in. A user who started signup but never completed
  it has `isRegistered = false` and cannot authenticate. `UserDetailServiceImpl` checks both
  `isEnabled` AND `isRegistered` before granting access.
- `AppUser.firstName` + `AppUser.lastName` replaced the old single `name` field. There is no `name`
  column in the database.

---

## Authentication Flows

### Sign-up (new user)

```
POST /user/signup-ticket            ← mobileNumber  →  sends OTP via SMS
POST /user/signup-ticket/validation ← mobileNumber + OTP  →  signupToken (UUID, short-lived)
POST /user/signup                   ← signupToken + firstName + lastName + password
```

The `signupToken` is a short-lived UUID stored in `SignupCacheService` (cache `SIGNUP_TOKEN`).
It proves the caller completed OTP verification before the `/user/signup` step.

### Check registration status (public)

```
POST /user/check-registration  ← mobileNumber  →  { isRegistered: true|false }
```

### Login with password

```
POST /user/login  ← mobileNumber + password  →  JWT
```

### Login with OTP (only registered users)

```
POST /user/login-ticket            ← mobileNumber  →  sends OTP (rejected if not isRegistered)
POST /user/login-ticket/validation ← mobileNumber + OTP  →  JWT
```

### Change password (authenticated)

```
POST /user/change-password  ← currentPassword + newPassword + confirmPassword
```

No OTP is required. The endpoint is protected by a valid JWT. The service verifies that
`newPassword` and `confirmPassword` match, then re-hashes and stores the new password.

---

## OTP Ticket Rules

Every OTP flow is **completely isolated** — signup and login have separate rate-limit counters,
block lists, and cache buckets. They cannot interfere with each other.

| Property                  | Config key                         | Default |
|---------------------------|------------------------------------|---------|
| OTP length                | `app.{flow}.ticket.length`         | 6       |
| OTP TTL                   | `app.{flow}.ticket.time-to-live`   | 2 min   |
| Block duration (on abuse) | `app.{flow}.ticket.block-duration` | 10 min  |
| Max failures before block | `app.{flow}.ticket.max-failure-count` | 5    |
| Resend cooldown           | enforced via `{flow}_LAST_TICKET` cache entry |

`{flow}` is either `signup` or `login`.

### Block semantics

When a mobile number exceeds `max-failure-count` failed OTP attempts it is added to
`BlockedMobileNumbersCacheService` with a TTL of `block-duration`. Any further ticket request from
that number is rejected with `TICKET_BLOCKED` until the block expires.

### Resend cooldown

After an OTP is sent, the send timestamp is stored in the `*_LAST_TICKET` cache entry with a TTL
equal to `time-to-live`. A new send request for the same mobile within that window is rejected with
`SEND_TICKET_TIME_LIMIT`.

---

## OTP Infrastructure — Class Map

```
CacheName enum
  SIGNUP_TICKET / SIGNUP_LAST_TICKET  ←  SignupTicketCacheService
  LOGIN_TICKET  / LOGIN_LAST_TICKET   ←  LoginTicketCacheService
         ↓                  ↓
  AbstractTicketCacheService  (all cache read/write ops)

  SignupProperties (app.signup.ticket.*)  ←  SignupTicketService
  LoginProperties  (app.login.ticket.*)   ←  LoginTicketService
         ↓                  ↓
  AbstractTicketService  (prepareTicket, sendTicketMessage, validateTicket)
```

### Adding a new OTP flow (e.g., "admin login")

1. Add two `CacheName` values: `ADMIN_TICKET`, `ADMIN_LAST_TICKET`.
2. Create `AdminTicketCacheService extends AbstractTicketCacheService`.
3. Create `AdminProperties` with a `TicketProperties ticket` field.
4. Create `AdminTicketService extends AbstractTicketService`.
5. Register `adminProperties()` bean in `EcommercePropertiesConfiguration` (prefix `"app.admin"`).
6. Add `app.admin.ticket.*` entries to `application.yml`.

---

## Shopping Cart (authenticated)

Every user has at most **one** cart (`cart.user_id` is unique); it is created lazily the first time
the user adds a product. A cart holds one line per product (`cart_item`, unique on
`(cart_id, product_id)`) with an integer `quantity`. The acting user is always taken from the JWT
principal — a user can only ever read or mutate their own cart.

| Method & path                          | Effect                                                           |
|----------------------------------------|------------------------------------------------------------------|
| `GET /cart`                            | Return the current cart (empty cart if none exists yet).         |
| `POST /cart/items`                     | Add `productId` (`quantity` defaults to 1); merges into the existing line if already present. |
| `PUT /cart/items/{productId}`          | Set the line quantity to an absolute value.                      |
| `POST /cart/items/{productId}/increment` | Increase the line quantity by 1.                               |
| `POST /cart/items/{productId}/decrement` | Decrease the line quantity by 1; the line is removed when it reaches 0. |
| `DELETE /cart/items/{productId}`       | Remove the product line.                                         |
| `DELETE /cart`                         | Clear all lines.                                                 |

Every response is the full cart: `items[]` (each with the product's `code`/`name`/`localName`,
`quantity`, `unitPrice`, `lineTotal`), plus `totalQuantity` and `totalPrice`. `unitPrice` is the
product's first price, preferring `discountPrice` when present.

**Guards** (all raise the standard error body):

- `PRODUCT_NOT_FOUND` — the product id does not exist.
- `PRODUCT_NOT_PURCHASABLE` (409) — the product is `INACTIVE` or `OUT_OF_STOCK`.
- `INSUFFICIENT_INVENTORY` (409) — the resulting quantity would exceed `product.inventory_count`.
- `CART_ITEM_NOT_FOUND` (404) — updating/incrementing/removing a product that is not in the cart
  (also returned when the cart itself does not exist yet).

---

## Error Response Contract

All errors return a JSON body:

```json
{
  "errorCode": "INVALID_TICKET",
  "message": "Ticket is not valid.",
  "errorParams": { }
}
```

- `errorCode` — enum name from `ECOMErrorType`.
- `message` — resolved from `messages*.properties` using the request's `Accept-Language` locale
  (falls back to the message key if the bundle does not have a translation).
- `errorParams` — optional structured data; for `VALIDATION_ERROR` this is a
  `{ "fieldName": ["message1", ...] }` map; `null` for most other error types.

Stack traces and internal exception details are **never included** in the response.

---

## Known Domain Gotchas

- **`changePassword` does not verify the current password.** `ChangePasswordRequestDto` carries
  only `newPassword` + `confirmPassword`; the service just checks they match.
- **`AppUser.mobile` must not be null.** It is set alongside `username` during signup. Every
  `findByMobile` lookup (OTP/login flows) breaks if this column is null.
- **`USER_LAST_TICKET` is gone** — it was renamed `SIGNUP_LAST_TICKET` in the May 2026 refactor.
  Any cache key with the old name is stale after a flush.
- **`TicketCacheService` was split** — the old single class no longer exists; use
  `SignupTicketCacheService` or `LoginTicketCacheService` depending on the flow.
