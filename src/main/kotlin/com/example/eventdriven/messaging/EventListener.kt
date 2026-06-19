package com.example.eventdriven.messaging

import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class EventListener {

    private val log = LoggerFactory.getLogger(EventListener::class.java)

    @RabbitListener(queues = ["\${app.messaging.queue}"])
    fun onEvent(event: EventMessage) {
        log.info("Received event: id={}, type={}, payload={}", event.id, event.type, event.payload)
    }
}
