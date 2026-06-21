package com.example.eventdriven.task.domain

import java.util.UUID

/**
 * Domain-level failure outcomes for Task operations. Used as the `Left` side of
 * [arrow.core.Either] returned by the inbound port, replacing thrown exceptions.
 */
sealed interface TaskError {
    data class NotFound(val id: UUID) : TaskError
}
