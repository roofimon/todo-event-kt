package com.example.eventdriven.domain.event

import com.example.eventdriven.domain.TaskStatus
import java.time.Instant
import java.util.UUID

/** Domain events raised by the Task aggregate. */
sealed interface TaskEvent : DomainEvent

data class TaskCreated(
    val taskId: UUID,
    val title: String,
    val status: TaskStatus,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : TaskEvent {
    override val aggregateId: UUID get() = taskId
    override val type: String get() = "task.created"
}

data class TaskStatusChanged(
    val taskId: UUID,
    val from: TaskStatus,
    val to: TaskStatus,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
) : TaskEvent {
    override val aggregateId: UUID get() = taskId
    override val type: String get() = "task.status-changed"
}
