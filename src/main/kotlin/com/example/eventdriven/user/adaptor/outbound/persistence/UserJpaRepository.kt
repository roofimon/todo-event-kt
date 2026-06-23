package com.example.eventdriven.user.adaptor.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/** Spring Data JPA repository over the `users` table. */
interface UserJpaRepository : JpaRepository<UserEntity, UUID>
