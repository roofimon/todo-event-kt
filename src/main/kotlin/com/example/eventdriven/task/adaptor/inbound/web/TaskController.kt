package com.example.eventdriven.task.adaptor.inbound.web

import com.example.eventdriven.task.domain.Task
import com.example.eventdriven.task.domain.TaskError
import com.example.eventdriven.task.domain.TaskStatus
import com.example.eventdriven.task.port.inbound.TaskUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
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
data class AssignRequest(val assigneeId: UUID)

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val service: TaskUseCase) {

    @PostMapping
    fun create(@RequestBody request: CreateTaskRequest): ResponseEntity<Task> {
        val task = service.create(request.title, request.description)
        return ResponseEntity.status(HttpStatus.CREATED).body(task)
    }

    @GetMapping
    fun list(): List<Task> = service.all()

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<Task> =
        service.get(id).fold({ ResponseEntity.notFound().build() }, { ResponseEntity.ok(it) })

    @PatchMapping("/{id}/status")
    fun changeStatus(
        @PathVariable id: UUID,
        @RequestBody request: ChangeStatusRequest,
    ): ResponseEntity<Task> =
        service.changeStatus(id, request.status)
            .fold({ ResponseEntity.notFound().build() }, { ResponseEntity.ok(it) })

    @PatchMapping("/{id}/assignee")
    fun assign(
        @PathVariable id: UUID,
        @RequestBody request: AssignRequest,
    ): ResponseEntity<Task> =
        service.assign(id, request.assigneeId).fold(
            { error ->
                when (error) {
                    is TaskError.AssigneeNotFound -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build()
                    else -> ResponseEntity.notFound().build()
                }
            },
            { ResponseEntity.ok(it) },
        )

    @DeleteMapping("/{id}/assignee")
    fun unassign(@PathVariable id: UUID): ResponseEntity<Task> =
        service.unassign(id).fold({ ResponseEntity.notFound().build() }, { ResponseEntity.ok(it) })
}
