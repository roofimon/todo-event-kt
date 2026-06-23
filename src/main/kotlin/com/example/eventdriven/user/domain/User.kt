package com.example.eventdriven.user.domain

import java.util.UUID

/** Core domain model representing a user that a task can be assigned to. */
data class User(
    val id: UUID,
    val name: String,
    val email: String? = null,
)
