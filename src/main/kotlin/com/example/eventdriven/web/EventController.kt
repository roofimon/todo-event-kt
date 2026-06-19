package com.example.eventdriven.web

import com.example.eventdriven.messaging.EventMessage
import com.example.eventdriven.messaging.EventPublisher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class CreateEventRequest(val type: String, val payload: String)

@RestController
@RequestMapping("/api/events")
class EventController(private val publisher: EventPublisher) {

    @PostMapping
    fun create(@RequestBody request: CreateEventRequest): ResponseEntity<EventMessage> {
        val event = EventMessage(
            id = UUID.randomUUID().toString(),
            type = request.type,
            payload = request.payload,
        )
        publisher.publish(event)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event)
    }
}
