package com.example.eventdriven.eventlog

import com.example.eventdriven.domain.event.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Consumer on the internal event bus. Its only responsibility is to append
 * each domain event to the dedicated event log file (see logback-spring.xml,
 * appender "DOMAIN_EVENTS" -> logs/domain-events.log).
 */
@Component
class DomainEventLogConsumer {

    private val eventLog = LoggerFactory.getLogger("domain-events")

    @EventListener
    fun on(event: DomainEvent) {
        //delay 1 second to demonstrate
        Thread.sleep(1000)
        eventLog.info(
            "type={} aggregateId={} eventId={} occurredAt={} event={}",
            event.type,
            event.aggregateId,
            event.eventId,
            event.occurredAt,
            event,
        )
    }
}
