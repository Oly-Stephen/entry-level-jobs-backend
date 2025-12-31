# Entry-Level Jobs API

Spring Boot service that aggregates entry-level job listings from public APIs and serves them with pagination.

## Environment variables

The application keeps all credentials out of version control. Before running locally (or in CI/CD), set the following environment variables or supply them via container/orchestration secrets:

| Variable                          | Description                                                                               |
| --------------------------------- | ----------------------------------------------------------------------------------------- |
| `SPRING_DATASOURCE_URL`           | JDBC URL for PostgreSQL. Example: `jdbc:postgresql://localhost:5432/entry_level_jobs_db`. |
| `SPRING_DATASOURCE_USERNAME`      | Database username.                                                                        |
| `SPRING_DATASOURCE_PASSWORD`      | Database password.                                                                        |
| `SECURITY_ADMIN_USERNAME`         | Bootstrap admin username for your auth layer.                                             |
| `SECURITY_ADMIN_PASSWORD`         | Bootstrap admin password.                                                                 |
| `SECURITY_JWT_SECRET`             | Signing secret for JWT tokens (strong random string).                                     |
| `SECURITY_JWT_EXPIRATION_SECONDS` | Optional override for token lifetime.                                                     |

### Windows PowerShell example

```powershell
# Run these once per shell session (or use setx to persist for your user profile)
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/entry_level_jobs_db"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "your-strong-password"
$env:SECURITY_ADMIN_USERNAME = "admin"
$env:SECURITY_ADMIN_PASSWORD = "another-strong-password"
$env:SECURITY_JWT_SECRET = "use-a-long-random-string"
```

You can also copy `src/main/resources/application-example.properties` to `src/main/resources/application.properties` (gitignored) for local development and replace the placeholder values. Never commit the real file.

## Build & run

```powershell
./mvnw.cmd clean package
./mvnw.cmd spring-boot:run
```

Ensure your PostgreSQL instance is running and accessible via the values supplied above.
