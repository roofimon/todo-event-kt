package com.example.eventdriven.infra.messaging

sealed interface MessagingError {
    data class BrokerUnavailable(val cause: Throwable) : MessagingError
    data class PublishFailed(val eventId: String, val cause: Throwable) : MessagingError
}
