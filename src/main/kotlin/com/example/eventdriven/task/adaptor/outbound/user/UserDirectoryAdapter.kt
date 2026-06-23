package com.example.eventdriven.task.adaptor.outbound.user

import com.example.eventdriven.task.port.outbound.UserDirectory
import com.example.eventdriven.user.port.outbound.UserRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Adapter implementing the Task feature's [UserDirectory] port by delegating to
 * the User feature's [UserRepository]. This is the single seam between the two
 * features.
 */
@Component
class UserDirectoryAdapter(private val users: UserRepository) : UserDirectory {

    override fun exists(userId: UUID): Boolean = users.findById(userId).isSome()
}
