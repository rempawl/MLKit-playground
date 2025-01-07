package com.rempawl.core.kotlin.progress

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.atomic.AtomicBoolean

interface ProgressSemaphore {
    val hasProgress: Flow<Boolean>
    fun addProgress()
    fun removeProgress()
}

fun <T> Flow<T>.watchProgress(counter: ProgressSemaphore): Flow<T> {
    return onStart { counter.addProgress() }
        .removeProgressOnAny(counter)
}

private fun <T> Flow<T>.removeProgressOnAny(counter: ProgressSemaphore): Flow<T> {
    val decreased = AtomicBoolean(false)
    fun decreaseCounter() {
        if (decreased.compareAndSet(false, true)) {
            counter.removeProgress()
        }
    }
    return this.onEach { decreaseCounter() }
        .onCompletion { decreaseCounter() }
}