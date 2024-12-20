package com.rempawl.core.kotlin.extensions

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

fun <T> delayFlow(timeout: Duration, value: T): Flow<T> = flow {
    delay(timeout)
    emit(value)
}