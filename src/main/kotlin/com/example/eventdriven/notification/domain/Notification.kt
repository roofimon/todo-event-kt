package com.example.eventdriven.notification.domain

import java.time.Instant
import java.util.UUID

/**
 * A user-facing notification, derived from domain events. Immutable; marking as
 * read returns a new instance.
 */
data class Notification(
    val id: UUID = UUID.randomUUID(),
    val recipientId: UUID,
    val type: String,
    val message: String,
    val read: Boolean = false,
    val createdAt: Instant = Instant.now(),
) {
    /** Marks the notification read. Already-read is a no-op and returns the same instance. */
    fun markRead(): Notification = if (read) this else copy(read = true)

    companion object {
        fun create(recipientId: UUID, type: String, message: String): Notification =
            Notification(recipientId = recipientId, type = type, message = message)
    }
}
