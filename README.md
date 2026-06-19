# event-driven

Spring Boot + Kotlin event-driven service skeleton.

- **Spring Boot** 4.1.0 / **Kotlin** 2.3.21 / **Java** 21 (Gradle Kotlin DSL)
- **Spring Web** (REST), **Spring AMQP** (RabbitMQ), **H2** (in-memory)

## Run

Start RabbitMQ (e.g. via Docker):

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Then run the app:

```bash
./gradlew bootRun
```

Connection settings are overridable via env vars: `RABBITMQ_HOST`, `RABBITMQ_PORT`,
`RABBITMQ_USER`, `RABBITMQ_PASSWORD` (defaults: `localhost:5672`, `guest`/`guest`).

## Try the event flow

Publish an event over REST; it is sent to the `events.exchange` topic exchange,
routed to `events.queue`, and logged by `EventListener`:

```bash
curl -X POST http://localhost:8080/api/events \
  -H 'Content-Type: application/json' \
  -d '{"type":"order.created","payload":"order-42"}'
```

Watch the app log for: `Received event: id=..., type=order.created, payload=order-42`.

## Domain events

The `Task` aggregate raises domain events that flow through an internal,
in-process bus — no broker involved:

```
TaskService.create() / changeStatus()
    → DomainEventBus.publish(TaskCreated | TaskStatusChanged)   [internal bus]
    → SpringDomainEventBus (ApplicationEventPublisher)
    → DomainEventLogConsumer.@EventListener
    → logs/domain-events.log                                    [consumer: file only]
```

The consumer's sole job is to append each event to `logs/domain-events.log`
(configured in `logback-spring.xml`, `additivity="false"` so events do not go
to the console). Exercise it via the Task endpoints and tail the file:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' -d '{"title":"write docs"}'
tail -f logs/domain-events.log
```

> The standalone RabbitMQ demo (`/api/events`) is independent and still uses the
> broker; only the Task domain-event path was switched to the log-file consumer.

## Other endpoints

- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:eventdriven`, user `sa`)
- RabbitMQ management UI: http://localhost:15672 (guest/guest)

## Layout

```
src/main/kotlin/com/example/eventdriven/
├── EventDrivenApplication.kt   # entry point
├── messaging/
│   ├── EventMessage.kt          # the event payload (JSON over AMQP)
│   ├── MessagingConfig.kt       # exchange, queue, binding, JSON converter
│   ├── EventPublisher.kt        # convertAndSend wrapper
│   └── EventListener.kt         # @RabbitListener consumer
└── web/
    └── EventController.kt        # POST /api/events -> publish
```

## Build & test

```bash
./gradlew build
```

## Frontend (Vue 3 + TypeScript)

A Vite-based web UI lives in `frontend/`. It lists tasks, creates them, and
changes their status — each action hits the Task REST API and triggers a
domain event on the backend.

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173 (proxies /api -> http://localhost:8080)
```

Run the Spring Boot app (`./gradlew bootRun`) alongside it. Other scripts:
`npm run build` (type-check + production build to `dist/`), `npm run type-check`.

```
frontend/src/
├── App.vue                 # page shell, loads/creates/updates tasks
├── api/tasks.ts            # typed fetch client for /api/tasks
├── types.ts                # Task / TaskStatus mirrors of the backend model
└── components/
    ├── TaskForm.vue        # create-task form
    └── TaskItem.vue        # task row with status selector
```
