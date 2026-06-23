package com.example.eventdriven.notification.application

import arrow.core.Either
import com.example.eventdriven.notification.domain.Notification
import com.example.eventdriven.notification.domain.NotificationError
import com.example.eventdriven.notification.port.inbound.NotificationUseCase
import com.example.eventdriven.notification.port.outbound.NotificationRepository
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Application service for the Notification aggregate. Implements the inbound
 * [NotificationUseCase] port and persists through the outbound
 * [NotificationRepository] port.
 */
@Service
class NotificationService(private val repository: NotificationRepository) : NotificationUseCase {

    override fun notify(recipientId: UUID, type: String, message: String): Notification =
        repository.save(Notification.create(recipientId = recipientId, type = type, message = message))

    override fun forRecipient(recipientId: UUID): List<Notification> =
        repository.findByRecipientId(recipientId)

    override fun markRead(id: UUID): Either<NotificationError, Notification> =
        repository.findById(id)
            .toEither { NotificationError.NotFound(id) }
            .map { it.markRead().also(repository::save) }
}
