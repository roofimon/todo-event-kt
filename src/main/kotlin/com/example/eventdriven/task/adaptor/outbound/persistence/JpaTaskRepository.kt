package com.example.eventdriven.task.adaptor.outbound.persistence

import arrow.core.Option
import com.example.eventdriven.task.domain.Task
import com.example.eventdriven.task.port.outbound.TaskRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Outbound adapter backing [TaskRepository] with the JPA `tasks` table, mapping
 * between [TaskEntity] and the [Task] domain model. The default; selected unless
 * `app.task.repository=in-memory`.
 */
@Repository
@ConditionalOnProperty(name = ["app.task.repository"], havingValue = "jpa", matchIfMissing = true)
class JpaTaskRepository(private val jpa: TaskJpaRepository) : TaskRepository {

    override fun save(task: Task): Task = jpa.save(TaskEntity.fromDomain(task)).toDomain()

    override fun findById(id: UUID): Option<Task> =
        Option.fromNullable(jpa.findById(id).orElse(null)?.toDomain())

    override fun findAll(): List<Task> = jpa.findAll().map { it.toDomain() }
}
