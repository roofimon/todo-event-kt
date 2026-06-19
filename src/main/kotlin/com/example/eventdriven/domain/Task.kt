package com.example.eventdriven.domain

import java.time.Instant
import java.util.UUID

/**
 * Core domain model representing a unit of work.
 */
data class Task(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    DONE,
    CANCELLED,
}
