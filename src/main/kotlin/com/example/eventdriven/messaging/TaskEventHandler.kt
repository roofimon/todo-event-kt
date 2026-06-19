package com.example.eventdriven.messaging

import com.example.eventdriven.domain.event.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Bridges in-process domain events onto the message broker as integration
 * events. Listens for any [DomainEvent] and republishes it over RabbitMQ.
 */
@Component
class TaskEventHandler(private val publisher: EventPublisher) {

    private val log = LoggerFactory.getLogger(TaskEventHandler::class.java)

    @EventListener
    fun on(event: DomainEvent) {
        log.info("Domain event raised: type={}, aggregateId={}", event.type, event.aggregateId)
        publisher.publish(
            EventMessage(
                id = event.eventId.toString(),
                type = event.type,
                payload = event.aggregateId.toString(),
                occurredAt = event.occurredAt,
            ),
        )
    }
}
