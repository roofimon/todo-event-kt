package com.example.eventdriven.user.adaptor.outbound.persistence

import com.example.eventdriven.user.domain.User
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * JPA mapping for [User]. Kept separate from the pure domain model; the
 * persistence adapter maps between the two.
 */
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    var id: UUID,
    var name: String,
    var email: String? = null,
) {
    fun toDomain(): User = User(id = id, name = name, email = email)

    companion object {
        fun fromDomain(user: User): UserEntity =
            UserEntity(id = user.id, name = user.name, email = user.email)
    }
}
