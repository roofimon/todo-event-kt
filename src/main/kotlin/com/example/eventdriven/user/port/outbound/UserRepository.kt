package com.example.eventdriven.user.port.outbound

import arrow.core.Option
import com.example.eventdriven.user.domain.User
import java.util.UUID

/**
 * Outbound (driven) port for persisting User aggregates. The application layer
 * depends on this contract; outbound adapters provide the implementation.
 */
interface UserRepository {
    fun save(user: User): User
    fun findById(id: UUID): Option<User>
    fun findAll(): List<User>
}
