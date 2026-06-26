package com.example.eventdriven.loadtest

import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.constantUsersPerSec
import io.gatling.javaapi.core.CoreDsl.global
import io.gatling.javaapi.core.CoreDsl.jsonPath
import io.gatling.javaapi.core.CoreDsl.rampUsersPerSec
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.time.Duration

/**
 * Load test for the Task/User/Notification REST API. Drives a realistic task
 * lifecycle under an open-model arrival rate, asserting latency + error rate.
 *
 * Prerequisites: `docker compose up -d` (Postgres + RabbitMQ) and `./gradlew bootRun`.
 * Run: `./gradlew gatlingRun` (override target with `BASE_URL=http://host:port`).
 */
class ApiSimulation : Simulation() {

    private val baseUrl = System.getenv("BASE_URL") ?: "http://localhost:8080"

    private val httpProtocol = http.baseUrl(baseUrl)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")

    private val lifecycle = scenario("Task lifecycle")
        // Pick a real, seeded user so the assignment validation passes.
        .exec(
            http("list users").get("/api/users")
                .check(status().`is`(200), jsonPath("$[0].id").saveAs("userId")),
        )
        .exec(
            http("create task").post("/api/tasks").body(StringBody("""{"title":"load"}"""))
                .check(status().`is`(201), jsonPath("$.id").saveAs("taskId")),
        )
        .exec(http("get task").get("/api/tasks/#{taskId}").check(status().`is`(200)))
        .exec(
            http("change status").patch("/api/tasks/#{taskId}/status")
                .body(StringBody("""{"status":"IN_PROGRESS"}"""))
                .check(status().`is`(200)),
        )
        .exec(
            http("assign").patch("/api/tasks/#{taskId}/assignee")
                .body(StringBody("""{"assigneeId":"#{userId}"}"""))
                .check(status().`is`(200)),
        )
        .exec(
            http("list notifications").get("/api/notifications?recipientId=#{userId}")
                .check(status().`is`(200)),
        )
        .exec(http("list tasks").get("/api/tasks").check(status().`is`(200)))

    init {
        setUp(
            lifecycle.injectOpen(
                rampUsersPerSec(1.0).to(20.0).during(Duration.ofSeconds(30)),
                constantUsersPerSec(20.0).during(Duration.ofSeconds(30)),
            ),
        )
            .protocols(httpProtocol)
            .assertions(
                // Write endpoints have a ~1s floor: DomainEventLogConsumer does a synchronous
                // Thread.sleep(1000) per domain event (create/status/assign), so p95 sits near 1s.
                // Threshold set above that floor; tighten it if that demo sleep is removed.
                global().responseTime().percentile(95.0).lt(1500),
                global().failedRequests().percent().lt(1.0),
            )
    }
}
