package com.example.eventdriven.task.domain

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TaskTest {

    @Test
    fun `create records a TaskCreated event`() {
        val task = Task.create(title = "Write tests", description = "for the aggregate")

        assertEquals("Write tests", task.title)
        assertEquals(TaskStatus.PENDING, task.status)

        val events = task.pullEvents()
        assertEquals(1, events.size)
        val created = events.single() as TaskCreated
        assertEquals(task.id, created.taskId)
        assertEquals(task.title, created.title)
        assertEquals(TaskStatus.PENDING, created.status)
        assertEquals(task.id, created.aggregateId)
    }

    @Test
    fun `changeStatus records a TaskStatusChanged event`() {
        val task = Task.create(title = "Ship it").also { it.pullEvents() }

        val updated = task.changeStatus(TaskStatus.IN_PROGRESS)

        assertEquals(TaskStatus.IN_PROGRESS, updated.status)
        assertTrue(updated.updatedAt >= task.updatedAt)

        val events = updated.pullEvents()
        assertEquals(1, events.size)
        val changed = events.single() as TaskStatusChanged
        assertEquals(task.id, changed.taskId)
        assertEquals(TaskStatus.PENDING, changed.from)
        assertEquals(TaskStatus.IN_PROGRESS, changed.to)
    }

    @Test
    fun `changeStatus to the same status is a no-op and raises no event`() {
        val task = Task.create(title = "Idempotent").also { it.pullEvents() }

        val result = task.changeStatus(TaskStatus.PENDING)

        assertSame(task, result)
        assertTrue(result.pullEvents().isEmpty())
    }

    @Test
    fun `assignTo records a TaskAssigned event`() {
        val task = Task.create(title = "Assign me").also { it.pullEvents() }
        val user = UUID.randomUUID()

        val assigned = task.assignTo(user)

        assertEquals(user, assigned.assigneeId)
        val events = assigned.pullEvents()
        assertEquals(1, events.size)
        val event = events.single() as TaskAssigned
        assertEquals(task.id, event.taskId)
        assertNull(event.from)
        assertEquals(user, event.to)
    }

    @Test
    fun `assignTo the same user is a no-op and raises no event`() {
        val user = UUID.randomUUID()
        val task = Task.create(title = "Idempotent assign").assignTo(user).also { it.pullEvents() }

        val result = task.assignTo(user)

        assertSame(task, result)
        assertTrue(result.pullEvents().isEmpty())
    }

    @Test
    fun `unassign records a TaskUnassigned event`() {
        val user = UUID.randomUUID()
        val task = Task.create(title = "Unassign me").assignTo(user).also { it.pullEvents() }

        val unassigned = task.unassign()

        assertNull(unassigned.assigneeId)
        val events = unassigned.pullEvents()
        assertEquals(1, events.size)
        val event = events.single() as TaskUnassigned
        assertEquals(task.id, event.taskId)
        assertEquals(user, event.from)
    }

    @Test
    fun `unassign an unassigned task is a no-op and raises no event`() {
        val task = Task.create(title = "Already free").also { it.pullEvents() }

        val result = task.unassign()

        assertSame(task, result)
        assertTrue(result.pullEvents().isEmpty())
    }

    @Test
    fun `pullEvents drains the buffer so a second call returns nothing`() {
        val task = Task.create(title = "Drain once")

        assertEquals(1, task.pullEvents().size)
        assertTrue(task.pullEvents().isEmpty())
    }

    @Test
    fun `pending events are excluded from equals and copy`() {
        val a = Task.create(title = "Equality")
        // Same identity and state, but `a` still holds a buffered event.
        val b = a.copy()

        assertEquals(a, b)
        // copy() starts with a fresh, empty event buffer.
        assertTrue(b.pullEvents().isEmpty())
        assertEquals(1, a.pullEvents().size)
    }

    @Test
    fun `description defaults to null`() {
        val task = Task.create(title = "No description")
        assertNull(task.description)
    }
}
