package com.example.eventdriven.notification.port.outbound

import arrow.core.Option
import com.example.eventdriven.notification.domain.Notification
import java.util.UUID

/**
 * Outbound (driven) port for persisting Notification aggregates. The application
 * layer depends on this contract; outbound adapters provide the implementation.
 */
interface NotificationRepository {
    fun save(notification: Notification): Notification
    fun findById(id: UUID): Option<Notification>
    fun findByRecipientId(recipientId: UUID): List<Notification>
}
