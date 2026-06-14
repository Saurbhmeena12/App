package com.learning.app.data.remote.api

import kotlinx.coroutines.delay
import kotlin.math.pow

class RetryPolicy {
    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_DELAY_MS = 1000L
        private const val MAX_DELAY_MS = 10000L
        private const val BACKOFF_MULTIPLIER = 2.0
    }

    suspend fun <T> executeWithRetry(
        operation: suspend () -> T
    ): T {
        var lastException: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                if (attempt < MAX_RETRIES - 1) {
                    val delayMs = (INITIAL_DELAY_MS * BACKOFF_MULTIPLIER.pow(attempt.toDouble())).toLong()
                        .coerceAtMost(MAX_DELAY_MS)
                    delay(delayMs)
                }
            }
        }

        throw lastException ?: Exception("Operation failed after $MAX_RETRIES retries")
    }
}

seal class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Timeout(message: String = "Request timeout") : NetworkException(message)
    class ConnectionError(message: String = "Connection error") : NetworkException(message)
    class ServerError(val code: Int, message: String = "Server error: $code") : NetworkException(message)
    class UnknownError(message: String = "Unknown error", cause: Throwable? = null) : NetworkException(message, cause)
}
