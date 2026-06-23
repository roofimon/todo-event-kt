package com.example.eventdriven.task.port.outbound

import java.util.UUID

/**
 * Outbound (driven) port through which the Task feature checks that an assignee
 * exists, without depending on the User feature's internals. An adapter bridges
 * this to the actual user store.
 */
interface UserDirectory {
    fun exists(userId: UUID): Boolean
}
