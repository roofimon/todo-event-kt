package com.example.eventdriven.user.application

import com.example.eventdriven.user.domain.User
import com.example.eventdriven.user.port.outbound.UserRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Seeds a fixed set of users at startup (only when the table is empty) so a
 * user can be selected for task assignment. Ids are derived deterministically
 * from the name, so they stay stable across restarts for manual testing.
 */
@Component
class UserSeeder(private val repository: UserRepository) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (repository.findAll().isNotEmpty()) return
        listOf("Alice", "Bob", "Carol").forEach { name ->
            repository.save(
                User(
                    id = UUID.nameUUIDFromBytes(name.toByteArray()),
                    name = name,
                    email = "${name.lowercase()}@example.com",
                ),
            )
        }
    }
}
