package com.example.eventdriven.messaging

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    @param:Value("\${app.messaging.exchange}") private val exchange: String,
    @param:Value("\${app.messaging.routing-key}") private val routingKey: String,
) {
    fun publish(event: EventMessage) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event)
    }
}
