package com.example.eventdriven.notification.adaptor.inbound.event

import com.example.eventdriven.notification.port.inbound.NotificationUseCase
import com.example.eventdriven.task.domain.TaskAssigned
import com.example.eventdriven.task.domain.TaskUnassigned
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Inbound event adapter: turns task assignment events on the internal bus into
 * notifications for the affected user. Notification is a downstream consumer of
 * task events (one-way dependency).
 */
@Component
class TaskEventNotificationConsumer(private val notifications: NotificationUseCase) {

    @EventListener
    fun on(event: TaskAssigned) {
        notifications.notify(
            recipientId = event.to,
            type = event.type,
            message = "You were assigned task ${event.taskId}",
        )
    }

    @EventListener
    fun on(event: TaskUnassigned) {
        notifications.notify(
            recipientId = event.from,
            type = event.type,
            message = "Task ${event.taskId} was unassigned from you",
        )
    }
}
