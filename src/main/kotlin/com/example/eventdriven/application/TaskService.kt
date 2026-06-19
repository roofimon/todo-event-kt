package com.example.eventdriven.application

import com.example.eventdriven.domain.Task
import com.example.eventdriven.domain.TaskStatus
import com.example.eventdriven.domain.event.DomainEventBus
import com.example.eventdriven.domain.event.TaskCreated
import com.example.eventdriven.domain.event.TaskStatusChanged
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Application service for the Task aggregate. Mutations raise domain events
 * onto the internal [DomainEventBus]; consumers react in-process.
 *
 * Storage is an in-memory map for now (no persistence layer wired yet).
 */
@Service
class TaskService(private val bus: DomainEventBus) {

    private val store = ConcurrentHashMap<UUID, Task>()

    fun create(title: String, description: String? = null): Task {
        val task = Task(title = title, description = description)
        store[task.id] = task
        bus.publish(TaskCreated(taskId = task.id, title = task.title, status = task.status))
        return task
    }

    fun changeStatus(id: UUID, to: TaskStatus): Task {
        val current = store[id] ?: throw NoSuchElementException("Task not found: $id")
        if (current.status == to) return current
        val updated = current.copy(status = to, updatedAt = Instant.now())
        store[id] = updated
        bus.publish(TaskStatusChanged(taskId = id, from = current.status, to = to))
        return updated
    }

    fun get(id: UUID): Task? = store[id]

    fun all(): List<Task> = store.values.toList()
}
