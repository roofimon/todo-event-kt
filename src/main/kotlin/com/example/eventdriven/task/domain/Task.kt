package com.example.eventdriven.task.domain

import com.example.eventdriven.infra.event.DomainEvent
import java.time.Instant
import java.util.UUID

/**
 * Core domain model representing a unit of work.
 *
 * The aggregate raises domain events for business-meaningful state transitions
 * and buffers them internally. The application service drains them via
 * [pullEvents] after persisting, then publishes them onto the event bus.
 *
 * Pending events are intentionally declared outside the primary constructor so
 * they are excluded from the data class's [equals]/[hashCode]/[copy] semantics —
 * they are transient bookkeeping, not part of the aggregate's identity or state.
 */
data class Task(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    private val pendingEvents = mutableListOf<DomainEvent>()

    private fun record(event: DomainEvent) {
        pendingEvents += event
    }

    /** Returns the buffered events and clears the buffer (drain). */
    fun pullEvents(): List<DomainEvent> {
        val drained: List<DomainEvent> = pendingEvents.toList()
        pendingEvents.clear()
        return drained
    }

    /**
     * Transitions to [to], recording a [TaskStatusChanged] event. A transition
     * to the current status is a no-op and raises no event.
     */
    fun changeStatus(to: TaskStatus): Task {
        if (status == to) return this
        return copy(status = to, updatedAt = Instant.now())
            .also { it.record(TaskStatusChanged(taskId = id, from = status, to = to)) }
    }

    companion object {
        /** Creates a new task, recording a [TaskCreated] event. */
        fun create(title: String, description: String? = null): Task =
            Task(title = title, description = description)
                .also { it.record(TaskCreated(taskId = it.id, title = it.title, status = it.status)) }
    }
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    DONE,
    CANCELLED,
}
