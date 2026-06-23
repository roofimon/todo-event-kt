package com.example.eventdriven.user.adaptor.inbound.web

import com.example.eventdriven.user.domain.User
import com.example.eventdriven.user.port.inbound.UserUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserUseCase) {

    @GetMapping
    fun list(): List<User> = service.all()
}
