package com.rempawl.mlkit_playground.core

import com.rempawl.image.processing.core.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DispatchersProviderImpl(
    override val io: CoroutineDispatcher = Dispatchers.IO,
    override val main: CoroutineDispatcher = Dispatchers.Main,
    override val default: CoroutineDispatcher = Dispatchers.Default
) : DispatchersProvider