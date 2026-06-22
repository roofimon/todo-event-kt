package com.example.eventdriven.infra.integration

import com.example.eventdriven.infra.event.DomainEvent
import com.example.eventdriven.infra.messaging.EventMessage
import com.example.eventdriven.infra.messaging.EventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Consumer on the internal event bus that republishes each domain event to RabbitMQ
 * via [com.example.eventdriven.infra.messaging.EventPublisher]. Publish failures are logged, not thrown, so a broker outage
 * never breaks the in-process domain flow.
 */
@Component
class DomainEventRabbitPublisher(private val publisher: EventPublisher) {

    private val log = LoggerFactory.getLogger(DomainEventRabbitPublisher::class.java)

    @EventListener
    fun on(event: DomainEvent) {
        val message = EventMessage(
            id = event.eventId.toString(),
            type = event.type,
            payload = event.toString(),
            occurredAt = event.occurredAt,
        )
        publisher.publish(message).onLeft { error ->
            log.warn(
                "Failed to publish domain event to RabbitMQ: type={} eventId={} error={}",
                event.type, event.eventId, error,
            )
        }
    }
}