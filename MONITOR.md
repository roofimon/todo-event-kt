# Monitoring database connections during a performance test

How to observe HikariCP / Postgres connection usage while the Gatling load test
(`./gradlew gatlingRun`) runs. Easiest first.

## 1. Postgres side — `pg_stat_activity` (no code changes)

Sample the server's real connections in a separate shell during the run:

```bash
while true; do
  docker compose exec -T postgres psql -U eventdriven -d eventdriven -tAc \
    "select now()::time(0),
            count(*) filter (where state='active')  as active,
            count(*) filter (where state='idle')    as idle,
            count(*)                                 as total
     from pg_stat_activity where datname='eventdriven';"
  sleep 1
done
```

- `total` ramps up to HikariCP's `maximumPoolSize` (default **10**); `active` rises with concurrency.
- Server ceiling: `SHOW max_connections;`.

## 2. HikariCP pool stats in the app log (one property)

Add to `application.properties` — the Hikari housekeeper logs
`Pool stats (total=, active=, idle=, waiting=)`:

```properties
logging.level.com.zaxxer.hikari.pool.HikariPool=DEBUG
```

- `waiting=` (threads blocked waiting for a connection) is the saturation signal.
- Default interval ~30s, so it's coarse for a ~60s run.

## 3. Actuator + Micrometer metrics (richest, needs a dependency)

Add `org.springframework.boot:spring-boot-starter-actuator`, then poll during the test:

```bash
curl -s localhost:8080/actuator/metrics/hikaricp.connections.active | jq '.measurements'
# also: hikaricp.connections.idle / .pending / .max / .acquire / .timeout
```

- `hikaricp.connections.pending` = requests waiting for a connection.
- `hikaricp.connections.acquire` (timer) = how long acquisition takes.
- With `micrometer-registry-prometheus`, scrape `/actuator/prometheus` into Grafana for a time series.
- **Caveat (this app):** the datasource is a custom `HikariDataSource` bean wrapped in
  `LazyConnectionDataSourceProxy` (`infra/persistence/LazyDataSourceConfig.kt`). Spring binds Hikari
  metrics to the `HikariDataSource` bean, so it should be detected — but verify `hikaricp.*` actually
  appears in `/actuator/metrics` after adding the dependency.

## Interpreting it for this app

The ~1s p95 in the load test comes from `DomainEventLogConsumer`'s `Thread.sleep(1000)`, which runs
**after** the repository call returns — *outside* any DB transaction. So the connection is released
quickly; the blocked resource is the **Tomcat request thread**, not the pool.

- Expect an **under-utilized pool** here: low `active`, `pending = 0`, despite high latency. That
  rules out the DB pool and points at the thread/sleep.
- If instead you see `pending > 0` or rising `acquire` time, the pool (`maximumPoolSize`) is the
  constraint to tune.

## Relevant current config (`application.properties`)

- `spring.datasource.hikari.minimum-idle=0` — no idle connections pre-created (lazy).
- `spring.datasource.hikari.initialization-fail-timeout=-1` — no connect at startup.
- `spring.datasource.hikari.connection-timeout=3000` — give up acquiring after 3s.
- `maximumPoolSize` — not set, so HikariCP default **10**.
