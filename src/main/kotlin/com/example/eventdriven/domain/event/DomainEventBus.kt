package com.example.eventdriven.domain.event

/**
 * Internal (in-process) event bus. Aggregates publish domain events here;
 * consumers subscribe to react to them. This keeps event production decoupled
 * from how events are ultimately handled.
 */
interface DomainEventBus {
    fun publish(event: DomainEvent)
}
