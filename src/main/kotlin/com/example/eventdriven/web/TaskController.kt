package com.example.eventdriven.web

import com.example.eventdriven.application.TaskService
import com.example.eventdriven.domain.Task
import com.example.eventdriven.domain.TaskStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class CreateTaskRequest(val title: String, val description: String? = null)
data class ChangeStatusRequest(val status: TaskStatus)

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val service: TaskService) {

    @PostMapping
    fun create(@RequestBody request: CreateTaskRequest): ResponseEntity<Task> {
        val task = service.create(request.title, request.description)
        return ResponseEntity.status(HttpStatus.CREATED).body(task)
    }

    @GetMapping
    fun list(): List<Task> = service.all()

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<Task> =
        service.get(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    @PatchMapping("/{id}/status")
    fun changeStatus(
        @PathVariable id: UUID,
        @RequestBody request: ChangeStatusRequest,
    ): ResponseEntity<Task> =
        try {
            ResponseEntity.ok(service.changeStatus(id, request.status))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
}
