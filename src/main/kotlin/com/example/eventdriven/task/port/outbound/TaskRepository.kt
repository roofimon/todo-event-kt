package com.example.eventdriven.task.port.outbound

import arrow.core.Option
import com.example.eventdriven.task.domain.Task
import java.util.UUID

/**
 * Outbound (driven) port for persisting Task aggregates. The application layer
 * depends on this contract; outbound adapters provide the implementation.
 */
interface TaskRepository {
    fun save(task: Task): Task
    fun findById(id: UUID): Option<Task>
    fun findAll(): List<Task>
}
