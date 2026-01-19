# Entry-Level Jobs API

Spring Boot service that aggregates entry-level job listings from public APIs and serves them with pagination.

## Environment variables

The application keeps all credentials out of version control. Before running locally (or in CI/CD), set the following environment variables or supply them via container/orchestration secrets:

| Variable                          | Description                                                                               |
| --------------------------------- | ----------------------------------------------------------------------------------------- |
| `SPRING_DATASOURCE_URL`           | JDBC URL for PostgreSQL. Example: `jdbc:postgresql://localhost:5432/entry_level_jobs_db`. |
| `SPRING_DATASOURCE_USERNAME`      | Database username.                                                                        |
| `SPRING_DATASOURCE_PASSWORD`      | Database password.                                                                        |
| `SECURITY_ADMIN_EMAIL`            | Bootstrap admin email for your auth layer.                                                |
| `SECURITY_ADMIN_PASSWORD`         | Bootstrap admin password.                                                                 |
| `SECURITY_JWT_SECRET`             | Signing secret for JWT tokens (strong random string).                                     |
| `SECURITY_JWT_EXPIRATION_SECONDS` | Optional override for token lifetime.                                                     |

### Windows PowerShell example

```powershell
# Run these once per shell session (or use setx to persist for your user profile)
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/entry_level_jobs_db"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "your-strong-password"
$env:SECURITY_ADMIN_EMAIL = "admin@example.com"
$env:SECURITY_ADMIN_PASSWORD = "another-strong-password"
$env:SECURITY_JWT_SECRET = "use-a-long-random-string"
```

You can also copy `src/main/resources/application-example.properties` to `src/main/resources/application.properties` (gitignored) for local development and replace the placeholder values. Never commit the real file.

### Railway/PostgreSQL production profile

- A committed prod configuration lives at [src/main/resources/application-prod.properties](src/main/resources/application-prod.properties). It activates when the `prod` profile is enabled and points the datasource at the Railway public proxy (`maglev.proxy.rlwy.net:18636`) with SSL required. Override the embedded username/password by exporting `SPRING_DATASOURCE_*` (or `DB_*`) environment variables in your deployment platform.
- To launch locally against Railway, run `./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod`, or set `SPRING_PROFILES_ACTIVE=prod` before starting the app. The same flag applies when packaging (`./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod`).

## Build & run

```powershell
./mvnw.cmd clean package
./mvnw.cmd spring-boot:run
```

Ensure your PostgreSQL instance is running and accessible via the values supplied above.

## Core API surface

| Area          | Endpoint(s)                                   | Notes                                                                                                |
| ------------- | --------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| Public feed   | `GET /api/jobs`                               | Supports `keyword`, `location`, `page`, `size`. Returns paginated jobs with classification metadata. |
| Locations     | `GET /api/jobs/locations`                     | Powers autocomplete. Provides both rich `options[]` and legacy `locations[]`.                        |
| User auth     | `POST /api/auth/register`, `/api/auth/login`  | Returns `AuthResponse { token, expiresAt, profile }`. Tokens carry `ROLE_USER`.                      |
| Saved jobs    | `/api/saved-jobs/**`                          | Requires user JWT.                                                                                   |
| Admin auth    | `POST /admin/login`                           | Uses `security.admin.*` creds and issues `ROLE_ADMIN` tokens.                                        |
| Admin stats   | `GET /api/jobs/stats`                         | `{ success, total_jobs, timestamp }` for dashboard cards.                                            |
| Admin actions | `POST /api/jobs/fetch`, `/api/jobs/test-save` | Return envelopes with `success`, `message`, and relevant counters/job details.                       |

All controllers set `@CrossOrigin("*")`, so frontend apps (e.g., Vite on port 5173) can call `http://localhost:8080` directly without an additional proxy.

For detailed filtering/location payloads see [docs/frontend-filtering-guide.md](docs/frontend-filtering-guide.md).

## Admin panel workflow

1. **Login** – `POST /admin/login` with the `security.admin.*` credentials. Store `token` + `expiresAt` and send `Authorization: Bearer <token>` for every admin call.
2. **Dashboard data** – Fetch `GET /api/jobs/stats` and `GET /api/jobs?page=0&size=30` in parallel. Both honor admin JWTs and include CORS headers.
3. **Actions** – Trigger `POST /api/jobs/fetch` to refresh external listings, or `POST /api/jobs/test-save` for diagnostics. Both responses include human-friendly `message` strings plus counters the UI can display.
4. **Errors** – Missing/expired tokens yield `401 {"error":"Authentication required"}`; insufficient roles return `403 {"error":"Access denied"}`. Business errors surface descriptive `error` or `message` fields—render them verbatim.
5. **Token lifetime** – Every auth response includes `expiresAt`. Frontend code (e.g., `AuthContext`) should schedule auto-logout based on that timestamp.

## Admin login & JWT debugging

- `POST http://localhost:8080/admin/login` expects the `security.admin.*` credentials configured in `application.properties` (or env variables). The endpoint is public; no token required.
- JWTs are signed with `security.jwt.secret`. If you rotate this value, restart the app and log in again to obtain a fresh token—the filter will reject any token signed with the old secret.
- Postman tip: clear the `Authorization` header when calling `/admin/login` to avoid sending an expired `Bearer` token from previous sessions. After the login response, copy the returned token into the header for subsequent admin requests (`Authorization: Bearer <token>`).
