# TeamFlow.AI - Backend

Multi-module Spring Boot / Spring Cloud microservices skeleton.

## Modules
- `common-lib` — shared entities, DTOs, exceptions, security and event contracts (library, no main class)
- `service-registry` — Eureka service registry (port 8761)
- `api-gateway` — edge gateway: routing, CORS, JWT rejection (port 8080)
- `identity-service` — auth, RBAC, org structure, leave (port 8081)
- `project-service` — projects, sprints, tasks, bugs, meetings (port 8082)
- `ai-service` — recommendations, health scoring, dashboards (port 8083)

## Build
```
mvn clean install
```

## Run a module
```
cd service-registry
mvn spring-boot:run
```

Start order for local development: `service-registry` → `api-gateway` → `identity-service` → `project-service` → `ai-service`.

Services other than `service-registry` expect MySQL, MongoDB, Redis and RabbitMQ reachable via the env vars declared in each module's `application.yml` (defaults point to `localhost`).

This is a structural skeleton only — business logic (entities, services, controllers, etc.) is committed separately into the existing package folders.
