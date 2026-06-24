package com.example.eventdriven.task.adaptor.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/** Spring Data JPA repository over the `tasks` table. */
interface TaskJpaRepository : JpaRepository<TaskEntity, UUID>
