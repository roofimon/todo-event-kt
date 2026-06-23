package com.example.eventdriven.notification.adaptor.outbound.persistence

import arrow.core.Option
import com.example.eventdriven.notification.domain.Notification
import com.example.eventdriven.notification.port.outbound.NotificationRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Outbound adapter backing [NotificationRepository] with an in-memory map. No
 * persistence layer is wired; swap this adapter to introduce one.
 */
@Repository
class InMemoryNotificationRepository : NotificationRepository {

    private val store = ConcurrentHashMap<UUID, Notification>()

    override fun save(notification: Notification): Notification {
        store[notification.id] = notification
        return notification
    }

    override fun findById(id: UUID): Option<Notification> = Option.fromNullable(store[id])

    override fun findByRecipientId(recipientId: UUID): List<Notification> =
        store.values.filter { it.recipientId == recipientId }
}
