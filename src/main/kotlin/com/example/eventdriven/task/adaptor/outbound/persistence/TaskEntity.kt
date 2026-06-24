package com.example.eventdriven.task.adaptor.outbound.persistence

import com.example.eventdriven.task.domain.Task
import com.example.eventdriven.task.domain.TaskStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * JPA mapping for the [Task] aggregate's persistent state. Kept separate from the
 * rich domain model (which also buffers domain events); the adapter maps between
 * the two. Loading from the DB produces a [Task] with no pending events.
 */
@Entity
@Table(name = "tasks")
class TaskEntity(
    @Id
    var id: UUID,
    var title: String,
    @Column(length = 1000)
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    var status: TaskStatus,
    var assigneeId: UUID? = null,
    var createdAt: Instant,
    var updatedAt: Instant,
) {
    fun toDomain(): Task = Task(
        id = id,
        title = title,
        description = description,
        status = status,
        assigneeId = assigneeId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            status = task.status,
            assigneeId = task.assigneeId,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
        )
    }
}
