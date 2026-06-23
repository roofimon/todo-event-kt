package com.example.eventdriven.notification.domain

import java.util.UUID

/**
 * Domain-level failure outcomes for Notification operations. Used as the `Left`
 * side of [arrow.core.Either] returned by the inbound port.
 */
sealed interface NotificationError {
    data class NotFound(val id: UUID) : NotificationError
}
