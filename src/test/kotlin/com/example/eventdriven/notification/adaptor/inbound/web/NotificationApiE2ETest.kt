package com.example.eventdriven.notification.adaptor.inbound.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end test driving the notification flow over real HTTP: assigning a task
 * to a user raises a TaskAssigned event, which the notification event adapter
 * turns into a notification for that user. Assertions are scoped to this test's
 * own task id so they are robust against a Spring context shared with other E2E
 * tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class NotificationApiE2ETest {

    @Autowired
    lateinit var rest: TestRestTemplate

    private fun jsonEntity(body: Map<String, Any?>): HttpEntity<Map<String, Any?>> {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        return HttpEntity(body, headers)
    }

    private fun notificationsFor(recipientId: String, taskId: String): List<NotificationResponse> {
        val all = rest.getForEntity(
            "/api/notifications?recipientId={r}",
            Array<NotificationResponse>::class.java,
            recipientId,
        )
        assertEquals(HttpStatus.OK, all.statusCode)
        return all.body!!.filter { it.message?.contains(taskId) == true }
    }

    @Test
    fun `assigning then unassigning a task notifies the user`() {
        val users = rest.getForEntity("/api/users", Array<UserResponse>::class.java)
        val recipientId = assertNotNull(users.body?.firstOrNull()?.id, "expected a seeded user")

        val created = rest.postForEntity(
            "/api/tasks",
            jsonEntity(mapOf("title" to "Notify me")),
            TaskResponse::class.java,
        )
        val taskId = assertNotNull(created.body?.id)

        // assign -> one task.assigned notification for the user
        rest.exchange(
            "/api/tasks/{id}/assignee",
            HttpMethod.PATCH,
            jsonEntity(mapOf("assigneeId" to recipientId)),
            TaskResponse::class.java,
            taskId,
        )

        val afterAssign = notificationsFor(recipientId, taskId)
        assertEquals(1, afterAssign.size, "exactly one notification for this task")
        val notification = afterAssign.single()
        assertEquals("task.assigned", notification.type)
        assertFalse(notification.read)
        val notificationId = assertNotNull(notification.id)

        // mark it read
        val read = rest.exchange(
            "/api/notifications/{id}/read",
            HttpMethod.PATCH,
            null,
            NotificationResponse::class.java,
            notificationId,
        )
        assertEquals(HttpStatus.OK, read.statusCode)
        assertTrue(read.body?.read == true)

        // unassign -> a second notification (task.unassigned) for the same user
        rest.exchange(
            "/api/tasks/{id}/assignee",
            HttpMethod.DELETE,
            null,
            TaskResponse::class.java,
            taskId,
        )

        val afterUnassign = notificationsFor(recipientId, taskId)
        assertEquals(2, afterUnassign.size)
        assertTrue(afterUnassign.any { it.type == "task.unassigned" })
    }

    @Test
    fun `mark read on unknown id returns 404`() {
        val response = rest.exchange(
            "/api/notifications/{id}/read",
            HttpMethod.PATCH,
            null,
            String::class.java,
            UUID.randomUUID(),
        )
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}

data class UserResponse(val id: String? = null)

data class TaskResponse(val id: String? = null)

data class NotificationResponse(
    val id: String? = null,
    val recipientId: String? = null,
    val type: String? = null,
    val message: String? = null,
    val read: Boolean = false,
)
