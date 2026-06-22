package com.example.eventdriven.infra.messaging

import arrow.core.Either
import org.springframework.amqp.rabbit.connection.Connection
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.stereotype.Component

/**
 * Models the RabbitMQ connection as a deferred effect. Reading [connect]`.value` is what
 * actually opens the broker connection — until then nothing connects, so the application
 * can boot without RabbitMQ being reachable.
 *
 * `lazy { }` memoizes the result; the happy-path [Connection] is a caching proxy that
 * reconnects internally, so reuse is fine. Note: a failed first access caches the `Left`
 * permanently — use `LazyThreadSafetyMode.NONE` with a re-evaluating wrapper if
 * recover-on-retry is desired.
 */
@Component
class LazyRabbitConnection(private val connectionFactory: ConnectionFactory) {

    val connect: Lazy<Either<MessagingError, Connection>> = lazy {
        Either.catch { connectionFactory.createConnection() }
            .mapLeft { MessagingError.BrokerUnavailable(it) }
    }
}
