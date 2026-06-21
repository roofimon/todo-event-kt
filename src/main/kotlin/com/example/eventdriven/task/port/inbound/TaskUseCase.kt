package com.example.eventdriven.task.port.inbound

import com.example.eventdriven.task.domain.Task
import com.example.eventdriven.task.domain.TaskStatus
import java.util.UUID

/**
 * Inbound (driving) port for the Task aggregate. Inbound adapters — e.g. the
 * web controller — depend on this contract rather than the concrete service.
 */
interface TaskUseCase {
    fun create(title: String, description: String? = null): Task
    fun changeStatus(id: UUID, to: TaskStatus): Task
    fun get(id: UUID): Task?
    fun all(): List<Task>
}
