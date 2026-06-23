package com.example.eventdriven.user.application

import arrow.core.Option
import com.example.eventdriven.user.domain.User
import com.example.eventdriven.user.port.inbound.UserUseCase
import com.example.eventdriven.user.port.outbound.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Application service for the User aggregate. Implements the inbound
 * [UserUseCase] port and reads through the outbound [UserRepository] port.
 */
@Service
class UserService(private val repository: UserRepository) : UserUseCase {

    override fun all(): List<User> = repository.findAll()

    override fun get(id: UUID): Option<User> = repository.findById(id)
}
