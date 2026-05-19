# Ecommerce

Spring Boot backend for an ecommerce/dashboard service. The project handles user signup/login with OTP verification, JWT authentication, merchant management, PostgreSQL persistence, and optional Redis-backed caching.

## Project Structure

```text
.
├── pom.xml                         # Parent Maven project
├── ecommerce-application/          # Spring Boot application and REST API
│   ├── src/main/java/.../controller
│   ├── src/main/java/.../service
│   ├── src/main/java/.../invoker   # External SMS and Shahkar clients
│   └── src/main/resources/config
└── ecommerce-persistence/          # Entities, repositories, and cache services
    └── src/main/java/.../persistence
```

## Modules

| Module | Purpose |
| --- | --- |
| `ecommerce-application` | Main Spring Boot app, REST controllers, security, business services, external HTTP clients |
| `ecommerce-persistence` | JPA entities, Spring Data repositories, Tedisson cache wrappers |

## Main Features

- Dashboard user registration using national code, mobile number, OTP ticket, and Shahkar validation.
- Login with username/password and JWT access tokens.
- Password reset flow using OTP ticket and temporary signup/reset token.
- Merchant CRUD for authenticated app users.
- Admin merchant management with owner reassignment, activation status, store URL, and bot token fields.
- PostgreSQL persistence with Hibernate/JPA.
- Ticket, signup-token, and blocked-mobile caches through Tedisson; Redis can be disabled for local cache mode.
- External integrations for SMS OTP delivery and Shahkar national-code/mobile matching.

## Technology Stack

- Java `21`
- Spring Boot `3.5.6`
- Maven multi-module build
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT via `jjwt`
- Lombok
- Tedisson Redis starter
- Tosan validation and HTTP server libraries

## Requirements

- JDK `21`
- Maven `3.9+` or compatible
- PostgreSQL
- Redis is optional; default config has `tedisson.redis.enabled=false`
- Valid SMS provider and Shahkar credentials for real signup flows

> Note: this project depends on `com.tosan.*` libraries. If they are not available from your Maven repositories, configure access to the required private/internal Maven repository before building.

## Configuration

Default configuration is in:

- `ecommerce-application/src/main/resources/config/application.properties`
- `ecommerce-application/src/main/resources/config/application-dev.properties`
- `ecommerce-application/src/main/resources/config/logback.xml`

Important properties:

| Property | Description | Default |
| --- | --- | --- |
| `server.port` | HTTP server port | `8080` |
| `spring.datasource.url` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/mydb` |
| `spring.datasource.username` | Database username | `mydb` |
| `spring.datasource.password` | Database password | `mydb` |
| `spring.jpa.hibernate.ddl-auto` | Hibernate schema strategy | `update` |
| `security.jwt.expiration-time` | JWT validity duration | `1h` |
| `security.jwt.secret-key` | Base64 secret used to sign JWTs | configured in properties |
| `signup.ticket.template-id` | SMS template ID for OTP | configured in properties |
| `signup.ticket.length` | OTP length | `4` |
| `signup.ticket.time-to-live` | OTP expiration duration | `2m` |
| `signup.ticket.block-duration` | Block duration after too many failures | `2m` |
| `signup.ticket.max-failure-count` | Max wrong OTP attempts before block | `5` |
| `signup.token-ttl` | Temporary signup/reset token lifetime | `2m` |
| `sms.base-url` | SMS provider base URL | configured in properties |
| `sms.api-key` | SMS provider API key | configured in properties |
| `shahkar.base-url` | Shahkar provider base URL | configured in properties |
| `shahkar.token` | Shahkar bearer token | configured in properties |
| `tedisson.redis.enabled` | Enable Redis-backed cache | `false` |
| `tedisson.redis.connection-type` | Redis deployment mode | `single_node` |
| `tedisson.redis.single-server.address` | Single Redis server address | `localhost:10501` |

Do not commit production secrets. Override sensitive values with environment variables, command-line arguments, or external Spring configuration.

Example environment variable overrides:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydb
export SPRING_DATASOURCE_USERNAME=mydb
export SPRING_DATASOURCE_PASSWORD=mydb
export SECURITY_JWT_SECRET_KEY=<base64-secret>
export SMS_API_KEY=<sms-api-key>
export SHAHKAR_TOKEN=<shahkar-token>
```

## Database

The application uses PostgreSQL and creates/updates schema automatically because `spring.jpa.hibernate.ddl-auto=update`.

Default expected database:

```text
host: localhost
port: 5432
database: mydb
username: mydb
password: mydb
```

Main tables/entities:

- `app_user`: dashboard users with username, password, mobile, role, and enabled flag.
- `merchant`: merchant profiles owned by app users.

User roles:

- `ROLE_APP_USER`
- `ROLE_ADMIN`

## Cache

Cache services are implemented in `ecommerce-persistence`:

- `SIGNUP_TICKET`: active OTP tickets.
- `USER_LAST_TICKET`: last sent ticket timestamps.
- `BLOCKED_MOBILE_NUMBER`: temporarily blocked mobile numbers.
- `SIGNUP_TOKEN`: temporary signup/reset tokens.

Redis is disabled by default:

```properties
tedisson.redis.enabled=false
```

When Redis is enabled, configure the matching Tedisson mode:

- `single_node`
- `cluster`
- `sentinel`
- `master_slave`

## External Services

### SMS

Used to send OTP tickets.

- Client: `SmsClient`
- Base property: `sms.base-url`
- Auth header: `x-api-key`
- Endpoint called by the app: `POST /v1/send/verify`
- Template parameters sent: `OTP`, `TIME`

### Shahkar

Used during signup to verify that national code and mobile number match.

- Client: `ShahkarClient`
- Base property: `shahkar.base-url`
- Auth header: `Authorization: Bearer <token>`
- Endpoint called by the app: `POST /services/inquiry/shahkar`

## Build and Run

From the repository root:

```bash
mvn clean package
```

Run the Spring Boot application:

```bash
mvn -pl ecommerce-application spring-boot:run
```

Or run the packaged jar:

```bash
java -jar ecommerce-application/target/ecommerce-application-0.0.1-SNAPSHOT.jar
```

Run unit tests with the `release` profile:

```bash
mvn test -Prelease
```

By default, the parent POM has Surefire configured with `skipTests=true`; use the `release` profile when you want tests to run.

## Authentication

Public endpoints do not require a token. Protected endpoints require:

```http
Authorization: Bearer <jwt-token>
```

Public endpoints:

- `POST /dashboard/user/signup-ticket`
- `POST /dashboard/user/credential-ticket/validation`
- `POST /dashboard/user/signup`
- `POST /dashboard/user/reset-credential-ticket`
- `POST /dashboard/user/reset-credential`
- `POST /dashboard/user/login`
- Swagger-related paths: `/v3/**`, `/swagger-ui.html`, `/swagger-ui/**`

CORS currently allows:

```text
http://localhost:8081
```

## API Overview

Base URL for local development:

```text
http://localhost:8080
```

### User APIs

#### Send signup ticket

```http
POST /dashboard/user/signup-ticket
Content-Type: application/json
```

Request:

```json
{
  "nationalCode": "0012345678",
  "mobileNumber": "09123456789"
}
```

Response:

```json
{
  "ticketTTLInSecond": 120
}
```

#### Validate credential ticket

```http
POST /dashboard/user/credential-ticket/validation
Content-Type: application/json
```

Request:

```json
{
  "ticket": "1234",
  "nationalCode": "0012345678",
  "mobileNumber": "09123456789"
}
```

Response:

```json
{
  "signupToken": "uuid-token",
  "newUser": true
}
```

#### Signup

```http
POST /dashboard/user/signup
Content-Type: application/json
```

Request:

```json
{
  "signupToken": "uuid-token",
  "password": "secure-password",
  "name": "Example User"
}
```

Response: empty body on success.

#### Send reset credential ticket

```http
POST /dashboard/user/reset-credential-ticket
Content-Type: application/json
```

Request:

```json
{
  "nationalCode": "0012345678"
}
```

Response:

```json
{
  "ticketTTLInSecond": 120,
  "mobileNumber": "09123456789"
}
```

#### Reset credential

```http
POST /dashboard/user/reset-credential
Content-Type: application/json
```

Request:

```json
{
  "signupToken": "uuid-token",
  "newPassword": "new-secure-password"
}
```

Response: empty body on success.

#### Login

```http
POST /dashboard/user/login
Content-Type: application/json
```

Request:

```json
{
  "username": "0012345678",
  "password": "secure-password"
}
```

Response:

```json
{
  "token": "jwt-token",
  "role": "ROLE_APP_USER"
}
```

### Merchant APIs for App Users

All endpoints require `ROLE_APP_USER`.

#### Create merchant

```http
POST /dashboard/merchant
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Request:

```json
{
  "title": "My Store",
  "description": "Store description",
  "base64EncodedLogo": "base64-logo"
}
```

Response: empty body on success.

#### Update own merchant

```http
PUT /dashboard/merchant/{merchantId}
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Request:

```json
{
  "title": "Updated Store",
  "description": "Updated description",
  "base64EncodedLogo": "base64-logo"
}
```

Response: empty body on success.

#### Disable own merchant

```http
PATCH /dashboard/merchant/{merchantId}/disable
Authorization: Bearer <jwt-token>
```

Response: empty body on success.

#### List own merchants

```http
GET /dashboard/merchant?pageNumber=0&pageSize=20
Authorization: Bearer <jwt-token>
```

Response:

```json
{
  "merchants": [
    {
      "id": 1,
      "title": "My Store",
      "description": "Store description",
      "isActive": true,
      "ownerUserId": 10,
      "storeUrl": null,
      "botToken": null,
      "base64EncodedLogo": "base64-logo",
      "createdAt": "2026-05-10T12:00:00.000+00:00",
      "updatedAt": "2026-05-10T12:00:00.000+00:00"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Pagination defaults:

- `pageNumber`: `0`
- `pageSize`: `20`
- maximum `pageSize`: `100`

### Merchant APIs for Admins

All endpoints require `ROLE_ADMIN`.

#### Update merchant as admin

```http
PUT /dashboard/admin/merchant/{merchantId}
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

Request:

```json
{
  "title": "Admin Updated Store",
  "description": "Updated by admin",
  "isActive": true,
  "ownerUserId": 10,
  "storeUrl": "https://example.com/store",
  "botToken": "telegram-bot-token",
  "base64EncodedLogo": "base64-logo"
}
```

Response:

```json
{
  "id": 1,
  "title": "Admin Updated Store",
  "description": "Updated by admin",
  "isActive": true,
  "ownerUserId": 10,
  "storeUrl": "https://example.com/store",
  "botToken": "telegram-bot-token",
  "base64EncodedLogo": "base64-logo",
  "createdAt": "2026-05-10T12:00:00.000+00:00",
  "updatedAt": "2026-05-10T12:30:00.000+00:00"
}
```

#### Delete merchant as admin

```http
DELETE /dashboard/admin/merchant/{merchantId}
Authorization: Bearer <jwt-token>
```

Response: empty body on success.

#### List merchants as admin

```http
GET /dashboard/admin/merchant?ownerUserId=10&isActive=true&pageNumber=0&pageSize=20
Authorization: Bearer <jwt-token>
```

Query parameters:

- `ownerUserId` optional
- `isActive` optional
- `pageNumber` optional
- `pageSize` optional

Response shape is the same as `GET /dashboard/merchant`.

## Signup Flow

1. Client calls `POST /dashboard/user/signup-ticket` with national code and mobile number.
2. Application generates a numeric OTP and sends it through the SMS provider.
3. Client calls `POST /dashboard/user/credential-ticket/validation` with national code, mobile number, and OTP.
4. Application returns a temporary `signupToken`.
5. Client calls `POST /dashboard/user/signup` with `signupToken`, password, and name.
6. Application validates national code/mobile ownership through Shahkar, creates `ROLE_APP_USER`, and deletes the temporary token.

## Password Reset Flow

1. Client calls `POST /dashboard/user/reset-credential-ticket` with national code.
2. Application sends an OTP to the mobile number saved for that user.
3. Client calls `POST /dashboard/user/credential-ticket/validation` with national code, mobile number, and OTP.
4. Application returns a temporary `signupToken`.
5. Client calls `POST /dashboard/user/reset-credential` with `signupToken` and `newPassword`.
6. Application updates the password and deletes the temporary token.

## Error Response

Errors are returned in this general shape:

```json
{
  "errorCode": "ERROR_CODE",
  "message": "Human readable message",
  "errorParams": {}
}
```

Common HTTP statuses:

- `400 Bad Request`: business or validation errors.
- `401 Unauthorized`: missing/invalid token, access denied, or authentication failure.
- `500 Internal Server Error`: unexpected errors or failed external service responses.

## Logging

Default Logback config is loaded from classpath:

```text
ecommerce-application/src/main/resources/config/logback.xml
```

If `config/logback.xml` exists next to the running process, it is used instead.

The current config logs to console by default. Rolling file and Logstash appenders are defined but commented out.

## Development Notes

- User `username` is the national code.
- Passwords are stored with BCrypt.
- JWT subject is the username/national code.
- App users can only update or disable their own merchants.
- Admin users can update, delete, list, and filter all merchants.
- Merchant logo is accepted as Base64 and limited by validation to `1024` bytes.
- Signup/reset tickets are deleted after successful validation.
- Too many invalid ticket attempts block the mobile number temporarily.

