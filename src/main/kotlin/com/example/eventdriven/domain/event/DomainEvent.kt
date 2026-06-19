package com.example.eventdriven.domain.event

import java.time.Instant
import java.util.UUID

/**
 * A domain event: something that has happened in the domain, expressed in
 * past tense. Domain events are immutable facts and are published in-process
 * via Spring's ApplicationEventPublisher.
 */
interface DomainEvent {
    /** Unique identity of this event occurrence. */
    val eventId: UUID

    /** Identity of the aggregate the event originates from. */
    val aggregateId: UUID

    /** When the event occurred. */
    val occurredAt: Instant

    /** Stable, transport-friendly event name, e.g. "task.created". */
    val type: String
}
