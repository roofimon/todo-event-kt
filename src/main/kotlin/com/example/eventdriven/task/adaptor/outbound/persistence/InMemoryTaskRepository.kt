package com.example.eventdriven.task.adaptor.outbound.persistence

import com.example.eventdriven.task.domain.Task
import com.example.eventdriven.task.port.outbound.TaskRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Outbound adapter backing [TaskRepository] with an in-memory map. No
 * persistence layer is wired yet; swap this adapter to introduce one.
 */
@Repository
class InMemoryTaskRepository : TaskRepository {

    private val store = ConcurrentHashMap<UUID, Task>()

    override fun save(task: Task): Task {
        store[task.id] = task
        return task
    }

    override fun findById(id: UUID): Task? = store[id]

    override fun findAll(): List<Task> = store.values.toList()
}
