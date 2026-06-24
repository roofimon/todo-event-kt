package com.example.eventdriven.notification.adaptor.outbound.persistence

import com.example.eventdriven.notification.domain.NotificationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/** Spring Data JPA repository over the `notifications` table. */
interface NotificationJpaRepository : JpaRepository<NotificationEntity, UUID> {
    fun findByRecipientId(recipientId: UUID): List<NotificationEntity>
}
