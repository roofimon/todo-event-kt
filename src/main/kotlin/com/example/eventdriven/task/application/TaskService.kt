package com.example.eventdriven.task.application

import arrow.core.Either
import arrow.core.Option
import com.example.eventdriven.infra.event.DomainEventBus
import com.example.eventdriven.task.domain.Task
import com.example.eventdriven.task.domain.TaskError
import com.example.eventdriven.task.domain.TaskStatus
import com.example.eventdriven.task.port.inbound.TaskUseCase
import com.example.eventdriven.task.port.outbound.TaskRepository
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Application service for the Task aggregate. Implements the inbound
 * [TaskUseCase] port, persists through the outbound [TaskRepository] port, and
 * raises domain events onto the internal [DomainEventBus]; consumers react
 * in-process.
 */
@Service
class TaskService(
    private val repository: TaskRepository,
    private val bus: DomainEventBus,
) : TaskUseCase {

    override fun create(title: String, description: String?): Task {
        val task = Task.create(title = title, description = description)
        repository.save(task)
        task.pullEvents().forEach(bus::publish)
        return task
    }

    override fun changeStatus(id: UUID, to: TaskStatus): Either<TaskError, Task> =
        repository.findById(id)
            .toEither { TaskError.NotFound(id) }
            .map { current ->
                current.changeStatus(to).also { updated ->
                    repository.save(updated)
                    updated.pullEvents().forEach(bus::publish)
                }
            }

    override fun get(id: UUID): Option<Task> = repository.findById(id)

    override fun all(): List<Task> = repository.findAll()
}
