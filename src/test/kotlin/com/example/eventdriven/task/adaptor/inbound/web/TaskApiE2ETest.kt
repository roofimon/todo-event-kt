package com.example.eventdriven.task.adaptor.inbound.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end test for the Task REST API. Starts the fully wired application on a
 * random port and drives it over real HTTP — exercising the whole stack: web
 * adapter -> use-case port -> service -> repository -> domain -> event bus, and
 * the Arrow Option/Either folds in the controller.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class TaskApiE2ETest {

    @Autowired
    lateinit var rest: TestRestTemplate

    private fun jsonEntity(body: Map<String, Any?>): HttpEntity<Map<String, Any?>> {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        return HttpEntity(body, headers)
    }

    @Test
    fun `full task lifecycle over HTTP`() {
        // create
        val created = rest.postForEntity(
            "/api/tasks",
            jsonEntity(mapOf("title" to "Write e2e", "description" to "drive the real API")),
            TaskResponse::class.java,
        )
        assertEquals(HttpStatus.CREATED, created.statusCode)
        val task = assertNotNull(created.body)
        val id = assertNotNull(task.id)
        assertEquals("Write e2e", task.title)
        assertEquals("PENDING", task.status)

        // get by id
        val fetched = rest.getForEntity("/api/tasks/{id}", TaskResponse::class.java, id)
        assertEquals(HttpStatus.OK, fetched.statusCode)
        assertEquals(id, fetched.body?.id)
        assertEquals("Write e2e", fetched.body?.title)

        // list contains it
        val list = rest.getForEntity("/api/tasks", Array<TaskResponse>::class.java)
        assertEquals(HttpStatus.OK, list.statusCode)
        assertTrue(list.body!!.any { it.id == id }, "task list should contain the created task")

        // change status (Arrow Either Right path)
        val patched = rest.exchange(
            "/api/tasks/{id}/status",
            HttpMethod.PATCH,
            jsonEntity(mapOf("status" to "IN_PROGRESS")),
            TaskResponse::class.java,
            id,
        )
        assertEquals(HttpStatus.OK, patched.statusCode)
        assertEquals("IN_PROGRESS", patched.body?.status)
    }

    @Test
    fun `GET unknown id returns 404`() {
        val response = rest.getForEntity("/api/tasks/{id}", TaskResponse::class.java, UUID.randomUUID())
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `PATCH unknown id returns 404`() {
        val response = rest.exchange(
            "/api/tasks/{id}/status",
            HttpMethod.PATCH,
            jsonEntity(mapOf("status" to "DONE")),
            TaskResponse::class.java,
            UUID.randomUUID(),
        )
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}

data class TaskResponse(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
)
