package com.rempawl.core.kotlin.error

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
data class UIError(
    val message: String,
    val duration: Duration,
) : AppError, java.io.Serializable

fun Throwable.toUIError(
    errorMessageProvider: ErrorMessageProvider,
    duration: Duration = DURATION_ERROR_LONG,
) = DefaultError(this).toUiError(errorMessageProvider, duration)

fun AppError.toUiError(
    errorMessageProvider: ErrorMessageProvider,
    duration: Duration = DURATION_ERROR_LONG,
) = UIError(
    message = errorMessageProvider.getErrorMessageFor(error = this),
    duration = duration
)

val DURATION_ERROR_LONG = 5.seconds
val DURATION_ERROR_SHORT = 3.seconds