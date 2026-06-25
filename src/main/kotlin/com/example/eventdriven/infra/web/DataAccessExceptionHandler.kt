package com.example.eventdriven.infra.web

import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.CannotCreateTransactionException
import org.springframework.transaction.TransactionException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.sql.SQLException

/**
 * Turns a Postgres connection failure into a clear console message and a clean
 * 503, instead of a default 500 + stack trace. The connection is lazy, so this
 * surfaces on the first repository call when Postgres is unreachable; the app
 * keeps running and recovers once Postgres is back.
 *
 * Covers every failure mode by walking the cause chain: cold connect (refused ->
 * ConnectException), unreachable host (connect timeout -> SocketTimeoutException
 * / DataAccessResourceFailureException), and a live connection dropped mid-query
 * (-> a connection-class SQLState). Genuine data errors fall through unchanged.
 */
@RestControllerAdvice
class DataAccessExceptionHandler {

    private val log = LoggerFactory.getLogger(DataAccessExceptionHandler::class.java)

    @ExceptionHandler(DataAccessException::class, TransactionException::class)
    fun onDataAccess(ex: Exception): ResponseEntity<String> {
        if (!isConnectionFailure(ex)) throw ex
        log.error("Failed to connect to Postgres: {}", NestedExceptionUtils.getMostSpecificCause(ex).message)
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Failed to connect")
    }

    private fun isConnectionFailure(ex: Throwable): Boolean {
        var cause: Throwable? = ex
        while (cause != null) {
            when {
                cause is DataAccessResourceFailureException -> return true
                cause is CannotCreateTransactionException -> return true
                cause is ConnectException -> return true
                cause is SocketTimeoutException -> return true
                // SQLState class 08 = connection exception; 57P0x = server shutting down / terminating connection.
                cause is SQLException && cause.sqlState?.let { it.startsWith("08") || it.startsWith("57P") } == true -> return true
            }
            cause = cause.cause
        }
        return false
    }
}
