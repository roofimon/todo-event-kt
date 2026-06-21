package com.example.eventdriven.infra.messaging

import java.time.Instant

/**
 * A simple domain event carried over RabbitMQ.
 */
data class EventMessage(
    val id: String,
    val type: String,
    val payload: String,
    val occurredAt: Instant = Instant.now(),
)
