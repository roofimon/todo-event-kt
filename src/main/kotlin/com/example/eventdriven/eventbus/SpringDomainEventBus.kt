package com.example.eventdriven.eventbus

import com.example.eventdriven.domain.event.DomainEvent
import com.example.eventdriven.domain.event.DomainEventBus
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * Internal bus backed by Spring's ApplicationEventPublisher. Events are
 * dispatched in-process to any @EventListener consumer.
 */
@Component
class SpringDomainEventBus(private val publisher: ApplicationEventPublisher) : DomainEventBus {
    override fun publish(event: DomainEvent) = publisher.publishEvent(event)
}
