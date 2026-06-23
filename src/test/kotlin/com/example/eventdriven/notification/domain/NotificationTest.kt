package com.example.eventdriven.notification.domain

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NotificationTest {

    @Test
    fun `create starts unread`() {
        val n = Notification.create(UUID.randomUUID(), "task.assigned", "hello")
        assertFalse(n.read)
        assertEquals("task.assigned", n.type)
        assertEquals("hello", n.message)
    }

    @Test
    fun `markRead sets read on a new instance`() {
        val n = Notification.create(UUID.randomUUID(), "task.assigned", "hello")

        val read = n.markRead()

        assertTrue(read.read)
        assertFalse(n.read)
        assertEquals(n.id, read.id)
    }

    @Test
    fun `markRead on an already-read notification is a no-op`() {
        val n = Notification.create(UUID.randomUUID(), "task.assigned", "hello").markRead()

        val result = n.markRead()

        assertSame(n, result)
    }
}
