package com.example.eventdriven.messaging

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MessagingConfig(
    @param:Value("\${app.messaging.exchange}") private val exchangeName: String,
    @param:Value("\${app.messaging.queue}") private val queueName: String,
    @param:Value("\${app.messaging.routing-key}") private val routingKey: String,
) {

    @Bean
    fun eventsExchange(): TopicExchange = TopicExchange(exchangeName)

    @Bean
    fun eventsQueue(): Queue = Queue(queueName, /* durable = */ true)

    @Bean
    fun eventsBinding(eventsQueue: Queue, eventsExchange: TopicExchange): Binding =
        BindingBuilder.bind(eventsQueue).to(eventsExchange).with(routingKey)

    @Bean
    fun messageConverter(): MessageConverter = JacksonJsonMessageConverter()

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory, converter: MessageConverter): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply { messageConverter = converter }
}
