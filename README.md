 # TeamFlow.AI — Backend

Internal project & workforce management platform for a single software company.
Spring Boot 4.1 · Java 21 · microservices. Signature feature: deterministic,
explainable Intelligent Workload Management & Smart Task Assignment — see
[ai-service](#ai-service) below.

> **Build status: verified ✅** — builds successfully with `mvn clean install`
> and runs end-to-end via Docker Compose. All five services register with
> Eureka and the demo walkthrough below has been executed successfully.

---

## Architecture

```
                        React client
                              │
                     ┌────────▼────────┐
                     │   api-gateway   │  :8080   routing · CORS · edge JWT check
                     └────────┬────────┘
                              │  (service ids resolved via Eureka)
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼────────┐  ┌─────────▼────────┐  ┌─────────▼────────┐
│identity-service│  │ project-service  │  │    ai-service    │
│     :8081      │  │      :8082       │  │      :8083       │
│ auth · RBAC    │  │ clients·projects │  │ workload scoring │
│ employee·dept  │  │ sprints·tasks    │  │ smart assignment │
│ team · leave   │  │ bugs·worklogs    │  │ alerts·dashboard │
│                │  │ meetings         │  │ notifications    │
└───────┬────────┘  └─────────┬────────┘  └─────────┬────────┘
        │  MySQL              │  MySQL              │  MongoDB
        └─────────────────────┴─────────────────────┘
                    RabbitMQ (async) · Redis (cache)
                              │
                     ┌────────▼────────┐
                     │ service-registry│  :8761   Eureka
                     └─────────────────┘
```

**Modules**

| Module | Port | Responsibility |
|---|---|---|
| `common-lib` | — | `BaseEntity`, `ApiResponse`, exceptions, JWT, event contracts. Shared by all services. |
| `service-registry` | 8761 | Eureka discovery. |
| `api-gateway` | 8080 | Single ingress; CORS, routing, first-line token rejection. |
| `identity-service` | 8081 | Authentication, RBAC, workforce graph (employee/department/team), two-stage leave approval. **Only service that issues tokens.** |
| `project-service` | 8082 | Delivery domain: clients, projects, sprints, tasks (comments/history/attachments, manual & smart assignment), bugs, work logs, meetings. |
| `ai-service` | 8083 | **Intelligent Workload Management**: deterministic workload scoring, smart-assignment & reassignment recommendations, manager alerts, dashboards, in-app notifications + email. MongoDB only — see [below](#ai-service). |

---

## Quick start

### Docker (whole stack)

```bash
cp .env.example .env
# edit .env — TEAMFLOW_JWT_SECRET has no default and startup fails without it
openssl rand -base64 48        # paste the result into .env

docker compose up --build
```

Then:

| What | Where |
|---|---|
| Eureka dashboard | http://localhost:8761 |
| Swagger (identity) | http://localhost:8081/swagger-ui.html |
| RabbitMQ console | http://localhost:15672 (guest/guest) |
| Gateway | http://localhost:8080 |

### Local (without Docker)

Requires JDK 21, Maven 3.9+, and MySQL / MongoDB / Redis / RabbitMQ running locally.

```bash
export TEAMFLOW_JWT_SECRET="$(openssl rand -base64 48)"
export MYSQL_PASSWORD=yourpassword

mvn clean install                      # build all modules

# start in this order, each in its own terminal
mvn -pl service-registry spring-boot:run
mvn -pl identity-service spring-boot:run -Dspring-boot.run.profiles=dev
mvn -pl project-service  spring-boot:run
mvn -pl ai-service       spring-boot:run
mvn -pl api-gateway      spring-boot:run
```

The `dev` profile additionally applies `db/demo`, seeding a demo tenant.

---

## Demo walkthrough

```bash
# 1 — sign in as the seeded administrator  (dev profile only)
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@teamflow.ai","password":"Admin@123"}'

# 2 — capture the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@teamflow.ai","password":"Admin@123"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["data"]["accessToken"])')

# 3 — a token minted by identity-service is accepted by project-service
curl -s http://localhost:8080/api/v1/projects -H "Authorization: Bearer $TOKEN"

# 4 — and by ai-service, which reports the live workload board (empty until
#     employees and tasks exist — see below)
curl -s http://localhost:8080/api/v1/dashboard/workload -H "Authorization: Bearer $TOKEN"

# 5 — create a department, an employee, a project and a task, then ask the
#     workload engine who should get it
curl -s -X POST http://localhost:8080/api/v1/departments -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"name":"Engineering","code":"ENG"}'

curl -s -X POST http://localhost:8080/api/v1/employees -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Priya","lastName":"Rao","workEmail":"priya@teamflow.ai","skills":["JAVA"]}'

curl -s -X POST http://localhost:8080/api/v1/projects -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Billing Revamp","code":"BILL","managerId":"<employee id from earlier step>"}'

curl -s -X POST http://localhost:8080/api/v1/tasks -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"projectId":"<project id>","title":"Design invoice schema","requiredSkills":["JAVA"]}'

curl -s http://localhost:8080/api/v1/tasks/<task id>/recommendations -H "Authorization: Bearer $TOKEN"
```

Seeded credentials are `admin@teamflow.ai` / `Admin@123` (SUPER_ADMIN). They exist
**only** under the `dev`/`docker` profiles — `prod` never loads `db/demo`. The
seeded admin has no linked HR record beyond the bootstrap employee, so most
write endpoints above need a second, non-admin employee created through
`/api/v1/employees` first if you want assignment flows to have someone to assign to.

---

## ai-service: Intelligent Workload Management

This is TeamFlow.AI's signature feature. Three design choices matter more than
the code:

**Deterministic, not machine learning.** Every score is a linear combination of
plain counts (active tasks, overdue tasks, remaining estimated hours, open bug
severity) against configurable weights in `WorkloadProperties`
(`teamflow.workload.*`). Nothing is hard-coded, and every recommendation carries
plain-language reasons a manager can check against the numbers themselves — see
`RecommendationService.buildReasons`.

**Event-sourced, not chatty.** ai-service never calls identity-service or
project-service synchronously to read employee/task/project/bug/meeting state.
It builds its own MongoDB read model (`EmployeeProfile`, `TaskSnapshot`, …) from
`EmployeeEvent`/`TaskEvent`/`ProjectEvent`/`BugEvent`/`MeetingEvent`/`LeaveEvent`
messages on RabbitMQ, published by whichever service owns that data. The one
exception is genuinely synchronous by necessity: a manager creating a task wants
ranked candidates *before* the task is even saved, so
`POST /api/v1/ai/recommendations/task-assignment` is a real Feign call from
project-service (see `AiRecommendationClient`), carrying the original caller's
JWT forward (see `FeignConfig` in project-service).

**Always fresh, never manually recalculated.** The score is computed from
current snapshot state on every request rather than cached and periodically
recomputed — so the score updates automatically whenever a task is
assigned/completed/reassigned, with no stale value to invalidate.

---

## Design decisions worth defending in a viva

**Identity and workforce data share one service.** Splitting them would put `User`
and `Employee` in separate databases despite being joined on nearly every request,
forcing chatty Feign calls for basic reads. Over-decomposition is the classic
microservices mistake; the bounded context is "who works here", and it is one
context. TeamFlow.AI is single-company software, so this service owns no
tenant/organization concept at all.

**Tokens are verified in every service, not just at the gateway.** Any process on the
cluster network can reach a service directly, so a gateway-only check protects
nothing. The gateway is a fast-fail optimisation, not the security boundary.

**Refresh tokens rotate and are stored hashed.** Redeeming one revokes it and issues a
new one, so a replayed token is already dead — which makes theft detectable rather
than silent. Only a SHA-256 digest is persisted, so a database leak yields nothing
usable. Presenting a revoked token revokes the user's entire session family.

**Login failures are indistinguishable.** Unknown email and wrong password return the
identical error, so the endpoint cannot enumerate registered addresses.

**Roles and permissions are rows, not enums.** The spec required configurable
permissions; an enum would need a redeploy to change what a role may do.

**Flyway owns the schema; Hibernate is set to `validate`.** With `ddl-auto: update` a
mapping/schema divergence is discovered in production. With `validate` it is a startup
failure.

**UUIDs are stored as `CHAR(36)`.** Hibernate's MySQL default of `BINARY(16)` is
unreadable in a console and cannot be pasted into a URL mid-demo.

**AI is rule-based behind an `AiProvider` interface.** Deterministic, needs no API key,
cannot fail on a network timeout during evaluation, and every recommendation is
explainable from the numbers that produced it. An LLM adapter drops in by config.

---

## Security

- BCrypt strength 12 (raised from the default 10 — ~250 ms/hash, negligible at login rates, materially slower to attack offline)
- Stateless JWT; 15-minute access tokens, 7-day rotating refresh tokens
- Account lockout after 5 failed attempts, 15 minutes, persisted in the database so a restart cannot clear it
- Method-level authorization via `@PreAuthorize` on roles *and* permissions
- Password policy enforced at the DTO: 8+ chars, upper, lower, digit, special
- Password change revokes every existing session
- No secret has a default value; services fail fast when one is missing
- Gateway strips inbound `X-User-*` headers before repopulating them from verified claims, so they cannot be spoofed

---

## Testing

```bash
mvn test                      # all modules
mvn -pl common-lib test       # one module
```

Present today: `TaskStatus` transition rules, score-banding boundaries, and a
`JwtService` suite covering claim round-trips, foreign signatures, expiry, and
refresh-vs-access typing. **Coverage is still below the 80% target** — none of
the Phase 2–4 business logic has unit tests yet. This is the next priority.

---

## What's implemented

All six services and every module in the product brief are implemented and
verified working end-to-end: auth, employee/department/team/leave, the full
project domain (clients, projects, sprints, tasks with comments/history/
attachments, bugs, work logs, meetings), the Intelligent Workload Management
engine (deterministic scoring, smart-assignment recommendations, reassignment
suggestions, manager alerts), dashboards, in-app notifications, and
best-effort email via Spring Mail.

## Known limitations

**What's deliberately out of scope for this version**, called out rather than
silently skipped:
- WebSocket push for notifications (REST + polling only — see `NotificationController`)
- "Department compatibility" and "project membership" as workload-scoring
  factors (would need additional event plumbing project-service doesn't
  currently publish)
- A `prod` Spring profile for project-service and ai-service (identity-service
  has one; the other two currently run under `default`/`docker` only)

**Test coverage** is limited to what shipped with the original scaffold
(`TaskStatus` transitions, score-banding boundaries, `JwtService`). None of the
new Phase 2–4 business logic has unit tests yet.

**No SRS, ER diagram, architecture diagram, or Postman collection** beyond
what's in this README.

`frontend/` from the original archive was empty and is not carried over.

### Suggested next steps

1. Add unit tests for the workload scorer and the task/leave status-transition
   rules — highest-value places for coverage given how much business logic
   they carry.
2. Add a `prod` profile for project-service and ai-service.
3. Add WebSocket push for notifications.

---

## Configuration reference

| Variable | Default | Notes |
|---|---|---|
| `TEAMFLOW_JWT_SECRET` | *none* | **Required.** 32+ bytes. Identical across all services. |
| `MYSQL_PASSWORD` | *none* | Required by identity and project services. |
| `MYSQL_HOST` / `MYSQL_PORT` | `localhost` / `3306` | |
| `MONGO_URI` | `mongodb://localhost:27017/...` | ai-service's read model and notifications. |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | |
| `SMTP_HOST` / `SMTP_PORT` | `localhost` / `1025` | Points at the bundled MailHog container under Docker; view sent mail at `http://localhost:8025`. |
| `MAIL_FROM` | `noreply@teamflow.ai` | |
| `EUREKA_SERVER_URL` | `http://localhost:8761/eureka/` | |
| `TEAMFLOW_AI_PROVIDER` | `rule-based` | |
| `TEAMFLOW_CORS_ORIGINS` | `localhost:3000,localhost:5173` | Never `*` — credentials are enabled. |

Every workload-scoring weight (`teamflow.workload.*` in ai-service's
`application.yml`) is also configurable — see `WorkloadProperties` for what
each one controls.

**Profiles:** `dev` (demo seed data, identity-service only) · `docker`
(container hostnames, all services) · `prod` (identity-service only today —
see limitation above).
