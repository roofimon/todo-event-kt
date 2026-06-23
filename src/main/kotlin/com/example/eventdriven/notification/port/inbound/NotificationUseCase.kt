package com.example.eventdriven.notification.port.inbound

import arrow.core.Either
import com.example.eventdriven.notification.domain.Notification
import com.example.eventdriven.notification.domain.NotificationError
import java.util.UUID

/**
 * Inbound (driving) port for the Notification aggregate. Driven by both the web
 * adapter (read/mark-read) and the event adapter (create via [notify]).
 */
interface NotificationUseCase {
    fun notify(recipientId: UUID, type: String, message: String): Notification
    fun forRecipient(recipientId: UUID): List<Notification>
    fun markRead(id: UUID): Either<NotificationError, Notification>
}
