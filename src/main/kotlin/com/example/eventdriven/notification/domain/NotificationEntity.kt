package com.example.eventdriven.notification.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * JPA mapping for [Notification]. The persistence adapter maps between this and
 * the [Notification] model. `read` maps to `is_read` since READ is a reserved
 * SQL word.
 */
@Entity
@Table(name = "notifications")
class NotificationEntity(
    @Id
    var id: UUID,
    var recipientId: UUID,
    var type: String,
    var message: String,
    @Column(name = "is_read")
    var read: Boolean = false,
    var createdAt: Instant,
) {
    fun toDomain(): Notification = Notification(
        id = id,
        recipientId = recipientId,
        type = type,
        message = message,
        read = read,
        createdAt = createdAt,
    )

    companion object {
        fun fromDomain(notification: Notification): NotificationEntity = NotificationEntity(
            id = notification.id,
            recipientId = notification.recipientId,
            type = notification.type,
            message = notification.message,
            read = notification.read,
            createdAt = notification.createdAt,
        )
    }
}
