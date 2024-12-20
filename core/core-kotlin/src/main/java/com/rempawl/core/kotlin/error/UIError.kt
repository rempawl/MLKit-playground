package com.rempawl.core.kotlin.error

import kotlin.time.Duration

data class UIError(
    val error: Throwable? = null,
    val duration: Duration,
) : AppError

fun Throwable.toUIError(duration: Duration) = UIError(this, duration)