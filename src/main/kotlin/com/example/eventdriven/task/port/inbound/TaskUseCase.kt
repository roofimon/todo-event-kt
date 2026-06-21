package com.example.eventdriven.task.port.inbound

import arrow.core.Either
import arrow.core.Option
import com.example.eventdriven.task.domain.Task
import com.example.eventdriven.task.domain.TaskError
import com.example.eventdriven.task.domain.TaskStatus
import java.util.UUID

/**
 * Inbound (driving) port for the Task aggregate. Inbound adapters — e.g. the
 * web controller — depend on this contract rather than the concrete service.
 */
interface TaskUseCase {
    fun create(title: String, description: String? = null): Task
    fun changeStatus(id: UUID, to: TaskStatus): Either<TaskError, Task>
    fun get(id: UUID): Option<Task>
    fun all(): List<Task>
}
