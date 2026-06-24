package com.example.eventdriven.task.adaptor.outbound.persistence

import arrow.core.Option
import com.example.eventdriven.task.domain.Task
import com.example.eventdriven.task.port.outbound.TaskRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Outbound adapter backing [TaskRepository] with an in-memory map. Selected when
 * `app.task.repository=in-memory`; otherwise the default [JpaTaskRepository] is used.
 */
@Repository
@ConditionalOnProperty(name = ["app.task.repository"], havingValue = "in-memory")
class InMemoryTaskRepository : TaskRepository {

    private val store = ConcurrentHashMap<UUID, Task>()

    override fun save(task: Task): Task {
        store[task.id] = task
        return task
    }

    override fun findById(id: UUID): Option<Task> = Option.fromNullable(store[id])

    override fun findAll(): List<Task> = store.values.toList()
}
