package com.example.eventdriven.infra.messaging

import arrow.core.Either
import arrow.core.flatMap
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    private val lazyConnection: LazyRabbitConnection,
    @param:Value("\${app.messaging.exchange}") private val exchange: String,
    @param:Value("\${app.messaging.routing-key}") private val routingKey: String,
) {
    fun publish(event: EventMessage): Either<MessagingError, Unit> =
        lazyConnection.connect().flatMap {
            Either.catch { rabbitTemplate.convertAndSend(exchange, routingKey, event) }
                .mapLeft { MessagingError.PublishFailed(event.id, it) }
        }
}
