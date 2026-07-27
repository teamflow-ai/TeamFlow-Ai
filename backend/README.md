# TeamFlow AI — Backend Foundation

Clean, production-ready Spring Boot foundation for the **TeamFlow AI** platform.

> **Status:** Part 1 — foundation only. No business features (auth, users, projects,
> tasks, AI) are implemented yet. This project establishes the base skeleton that
> future modules will build on.

## Tech Stack

| Layer            | Technology              |
|------------------|--------------------------|
| Language         | Java 25                 |
| Framework        | Spring Boot 4            |
| Build Tool       | Maven                    |
| Database         | MySQL 8                  |
| Persistence      | Spring Data JPA           |
| Validation       | Jakarta Bean Validation   |
| Security         | Spring Security (dependency only, skeleton config) |
| API Docs         | SpringDoc OpenAPI / Swagger UI |
| Boilerplate      | Lombok                   |
| Containerization | Docker / Docker Compose  |

## Project Structure

```
com.teamflow.ai
├── config          # OpenAPI, Swagger, CORS configuration
├── controller      # REST controllers (empty — future modules)
├── service         # Business services (empty — future modules)
├── repository      # Spring Data JPA repositories (empty — future modules)
├── entity          # BaseEntity, AuditableEntity
├── dto             # BaseResponse, ApiResponse
├── exception       # GlobalExceptionHandler
├── security        # SecurityConfig (skeleton)
├── util            # Shared utilities (empty — future modules)
└── validation      # Custom validators (empty — future modules)
```

## Prerequisites

- JDK 25
- Maven (or use the included `mvnw` wrapper)
- MySQL 8 (or Docker)

## Running Locally

1. Start MySQL (or run `docker compose up mysql -d`).
2. Configure environment variables (or rely on defaults in `application.yml`):

   ```
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=teamflow_ai
   DB_USERNAME=root
   DB_PASSWORD=root
   ```

3. Build and run:

   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. The app starts on **http://localhost:8080**.

## Running with Docker Compose

```bash
docker compose up --build
```

This starts both the MySQL database and the backend application.

## API Documentation

Once running, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI spec:

```
http://localhost:8080/v3/api-docs
```

## Health Check

```
http://localhost:8080/actuator/health
```

## Notes

- `SecurityConfig` currently permits all requests. It is a placeholder that will be
  replaced/extended when the authentication module is implemented.
- `ddl-auto` is set to `update` for development convenience; use migrations
  (e.g. Flyway/Liquibase) in production.

---

**BACKEND FOUNDATION COMPLETE — READY FOR AUTHENTICATION MODULE**
