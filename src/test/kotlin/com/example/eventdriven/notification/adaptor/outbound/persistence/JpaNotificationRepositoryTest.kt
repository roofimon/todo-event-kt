package com.example.eventdriven.notification.adaptor.outbound.persistence

import com.example.eventdriven.notification.domain.Notification
import com.example.eventdriven.notification.port.outbound.NotificationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Verifies the JPA-backed [JpaNotificationRepository] adapter is selected when
 * `app.notification.repository=jpa`, and that entity mapping (incl. the `is_read`
 * column for the reserved word) and the derived findByRecipientId query work
 * through the [NotificationRepository] port. Runs on H2 (test profile).
 */
@SpringBootTest
@TestPropertySource(properties = ["app.notification.repository=jpa"])
class JpaNotificationRepositoryTest {

    @Autowired
    lateinit var repository: NotificationRepository

    @Test
    fun `the jpa adapter is wired when the property selects it`() {
        assertIs<JpaNotificationRepository>(repository)
    }

    @Test
    fun `saves, finds by id, and lists by recipient`() {
        val alice = UUID.randomUUID()
        val bob = UUID.randomUUID()
        val saved = repository.save(Notification.create(alice, "task.assigned", "hello").markRead())
        repository.save(Notification.create(bob, "task.assigned", "other"))

        val found = repository.findById(saved.id)
        assertEquals(true, found.getOrNull()?.read)

        val forAlice = repository.findByRecipientId(alice)
        assertEquals(1, forAlice.size)
        assertEquals(alice, forAlice.single().recipientId)
    }
}
