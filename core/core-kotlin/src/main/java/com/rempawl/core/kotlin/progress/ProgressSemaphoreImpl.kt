package com.rempawl.core.kotlin.progress

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ProgressSemaphoreImpl : ProgressSemaphore {
    private val progressCounter = MutableStateFlow(0)

    override val hasProgress: Flow<Boolean>
        get() = progressCounter.map { it > 0 }.distinctUntilChanged()

    override fun addProgress() {
        progressCounter.update { it + 1 }
    }

    override fun removeProgress() {
        progressCounter.update { it - 1 }
    }
}