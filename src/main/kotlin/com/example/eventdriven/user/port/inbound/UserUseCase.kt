package com.example.eventdriven.user.port.inbound

import arrow.core.Option
import com.example.eventdriven.user.domain.User
import java.util.UUID

/**
 * Inbound (driving) port for the User aggregate. Inbound adapters — e.g. the
 * web controller — depend on this contract rather than the concrete service.
 */
interface UserUseCase {
    fun all(): List<User>
    fun get(id: UUID): Option<User>
}
