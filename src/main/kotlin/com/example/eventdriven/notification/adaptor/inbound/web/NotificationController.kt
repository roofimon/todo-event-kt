package com.example.eventdriven.notification.adaptor.inbound.web

import com.example.eventdriven.notification.domain.Notification
import com.example.eventdriven.notification.port.inbound.NotificationUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
class NotificationController(private val service: NotificationUseCase) {

    @GetMapping
    fun list(@RequestParam recipientId: UUID): List<Notification> = service.forRecipient(recipientId)

    @PatchMapping("/{id}/read")
    fun markRead(@PathVariable id: UUID): ResponseEntity<Notification> =
        service.markRead(id).fold({ ResponseEntity.notFound().build() }, { ResponseEntity.ok(it) })
}
