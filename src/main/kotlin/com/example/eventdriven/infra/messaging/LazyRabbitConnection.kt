package com.example.eventdriven.infra.messaging

import arrow.core.Either
import org.springframework.amqp.rabbit.connection.Connection
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.stereotype.Component

/**
 * Models acquiring the RabbitMQ connection as a deferred effect: nothing connects
 * until [connect] is invoked, so the application can boot without RabbitMQ reachable.
 *
 * Each invocation re-attempts the connection, so the publisher recovers automatically
 * once the broker comes back (a failure is never cached). The live connection itself is
 * cached by the underlying [ConnectionFactory] (a caching factory), so repeated calls are
 * cheap once established and only actually reconnect when needed.
 */
@Component
class LazyRabbitConnection(private val connectionFactory: ConnectionFactory) {

    fun connect(): Either<MessagingError, Connection> =
        Either.catch { connectionFactory.createConnection() }
            .mapLeft { MessagingError.BrokerUnavailable(it) }
}
