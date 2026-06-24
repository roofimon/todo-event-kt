package com.example.eventdriven.notification.adaptor.outbound.persistence

import arrow.core.Option
import com.example.eventdriven.notification.domain.Notification
import com.example.eventdriven.notification.domain.NotificationEntity
import com.example.eventdriven.notification.port.outbound.NotificationRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Outbound adapter backing [NotificationRepository] with the JPA `notifications`
 * table, mapping between [NotificationEntity] and the [Notification] domain model.
 * The default; selected unless `app.notification.repository=in-memory`.
 */
@Repository
@ConditionalOnProperty(name = ["app.notification.repository"], havingValue = "jpa", matchIfMissing = true)
class JpaNotificationRepository(private val jpa: NotificationJpaRepository) : NotificationRepository {

    override fun save(notification: Notification): Notification =
        jpa.save(NotificationEntity.fromDomain(notification)).toDomain()

    override fun findById(id: UUID): Option<Notification> =
        Option.fromNullable(jpa.findById(id).orElse(null)?.toDomain())

    override fun findByRecipientId(recipientId: UUID): List<Notification> =
        jpa.findByRecipientId(recipientId).map { it.toDomain() }
}
