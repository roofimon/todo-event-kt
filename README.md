# event-driven

A Spring Boot + Kotlin event-driven service built with hexagonal architecture, functional
error handling (Arrow), and **lazy ("connect on first use") connections** to both RabbitMQ and
Postgres — the app boots even when neither is reachable.

- **Spring Boot** 4.1 / **Kotlin** 2.3 / **Java** 21 (Gradle Kotlin DSL)
- **Spring Web** (REST), **Spring AMQP** (RabbitMQ), **Spring Data JPA**
- **Arrow** `Either`/`Option` in ports and services (no exceptions across boundaries)
- **Postgres** at runtime (Docker), **H2** in-memory for tests

## Features

Three feature modules, each hexagonal (`domain` / `port` / `application` / `adaptor`):

- **task** — create tasks, change status, assign/unassign a user. In-memory repository. Raises
  domain events (`TaskCreated`, `TaskStatusChanged`, `TaskAssigned`, `TaskUnassigned`).
- **user** — a Postgres-backed user table, seeded with Alice/Bob/Carol. Tasks are assigned to a
  user selected from it.
- **notification** — created **event-driven**: a consumer on the internal bus reacts to
  `TaskAssigned`/`TaskUnassigned` and records a notification for the affected user.

## Run

The app uses lazy connections, so it starts without RabbitMQ or Postgres up; each connects on first
use. To exercise persistence and messaging, start the dependencies first:

```bash
docker compose up -d        # postgres:17 (:5432) + rabbitmq:3-management (:5672, UI :15672)
```

Postgres provisions the schema + seed users on first start; RabbitMQ is optional — domain events are
republished to it, and the API degrades gracefully (logged warning) if it's down.

**The app:**

```bash
./gradlew bootRun           # http://localhost:8080
```

RabbitMQ settings are overridable via `RABBITMQ_HOST`/`RABBITMQ_PORT`/`RABBITMQ_USER`/`RABBITMQ_PASSWORD`
(defaults `localhost:5672`, `guest`/`guest`).

## REST API

**Tasks**

```bash
curl -X POST localhost:8080/api/tasks -H 'Content-Type: application/json' \
  -d '{"title":"write docs","description":"the README"}'
curl localhost:8080/api/tasks
curl localhost:8080/api/tasks/{id}
curl -X PATCH localhost:8080/api/tasks/{id}/status   -H 'Content-Type: application/json' -d '{"status":"IN_PROGRESS"}'
curl -X PATCH localhost:8080/api/tasks/{id}/assignee -H 'Content-Type: application/json' -d '{"assigneeId":"<user-id>"}'
curl -X DELETE localhost:8080/api/tasks/{id}/assignee     # unassign
```

Assigning to a user not in the table → `422`. Unknown task → `404`.

**Users / Notifications**

```bash
curl localhost:8080/api/users                                   # pick an id to assign
curl 'localhost:8080/api/notifications?recipientId=<user-id>'   # notifications for a user
curl -X PATCH localhost:8080/api/notifications/{id}/read        # mark read
```

Assigning a task to a user creates a `task.assigned` notification for them; unassigning creates a
`task.unassigned` one — visible immediately via the notifications endpoint.

**Standalone AMQP demo** — publish straight to RabbitMQ (independent of the domain-event flow):

```bash
curl -X POST localhost:8080/api/events -H 'Content-Type: application/json' \
  -d '{"type":"order.created","payload":"order-42"}'
```

## Event flow

Domain events flow in-process through an internal bus to several independent consumers:

```
TaskService.assign() / changeStatus() / ...
  → DomainEventBus.publish(TaskAssigned | TaskStatusChanged | ...)     [internal bus]
  → SpringDomainEventBus (ApplicationEventPublisher)
      ├─ DomainEventLogConsumer          → logs/domain-events.log
      ├─ DomainEventRabbitPublisher      → RabbitMQ events.exchange     [lazy connection]
      └─ TaskEventNotificationConsumer   → creates a Notification
```

`logs/domain-events.log` is configured in `logback-spring.xml` (`additivity="false"`, so events
don't hit the console). Tail it while hitting the Task endpoints.

## Lazy connections

Both external connections are deferred so the app boots with **zero** connections:

- **RabbitMQ** — `LazyRabbitConnection` defers `createConnection()`; the listener's auto-startup is
  off. The first publish connects; a broker outage is a logged warning, not a failure.
- **Postgres** — `LazyConnectionDataSourceProxy` + Hikari `minimum-idle=0` + `ddl-auto=none` +
  Hibernate boot metadata access disabled. The schema/seed are provisioned by the Postgres container
  (`docker/postgres/init.sql`), so the app opens its first DB connection only on the first repository
  call.

## Build & test

```bash
./gradlew build
```

Tests run on in-memory **H2** via the `test` Spring profile (`src/test/resources/application-test.properties`)
— no Docker required. Coverage includes domain unit tests and full-stack REST E2E tests
(`TaskApiE2ETest`, `NotificationApiE2ETest`).

Notification storage is switchable: `app.notification.repository=jpa` (default, Postgres) or
`in-memory`.

## Layout

```
src/main/kotlin/com/example/eventdriven/
├── task/            # feature: domain / port.{inbound,outbound} / application / adaptor.{inbound.web,outbound}
├── user/            # feature: Postgres-backed user directory
├── notification/    # feature: event-driven notifications (JPA or in-memory)
└── infra/
    ├── event/         # DomainEvent, DomainEventBus, SpringDomainEventBus (internal bus)
    ├── eventlog/      # DomainEventLogConsumer → log file
    ├── integration/   # DomainEventRabbitPublisher → RabbitMQ
    ├── messaging/     # RabbitMQ config, lazy connection, EventPublisher/Listener, /api/events
    ├── persistence/   # LazyDataSourceConfig (lazy Postgres connection)
    └── web/           # EventController (standalone AMQP demo)

docker-compose.yml          # Postgres + init.sql (schema & seed)
docker/postgres/init.sql
```

## Frontend (Vue 3 + TypeScript)

A Vite web UI in `frontend/` lists tasks, creates them, changes status, and **assigns a user**
(dropdown populated from `/api/users`). Each action hits the REST API and triggers the domain-event
flow.

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173 (proxies /api -> http://localhost:8080)
npm run build      # type-check + production build
npm run test:e2e   # Playwright tests (mock the API)
```

Run the backend (`./gradlew bootRun`) alongside it.
