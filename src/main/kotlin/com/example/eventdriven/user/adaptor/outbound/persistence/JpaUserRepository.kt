package com.example.eventdriven.user.adaptor.outbound.persistence

import arrow.core.Option
import com.example.eventdriven.user.domain.User
import com.example.eventdriven.user.port.outbound.UserRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Outbound adapter backing [UserRepository] with the JPA `users` table,
 * mapping between [UserEntity] and the [User] domain model.
 */
@Repository
class JpaUserRepository(private val jpa: UserJpaRepository) : UserRepository {

    override fun save(user: User): User =
        jpa.save(UserEntity.fromDomain(user)).toDomain()

    override fun findById(id: UUID): Option<User> =
        Option.fromNullable(jpa.findById(id).orElse(null)?.toDomain())

    override fun findAll(): List<User> = jpa.findAll().map { it.toDomain() }
}
