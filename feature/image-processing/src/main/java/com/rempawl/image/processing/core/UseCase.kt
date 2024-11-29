package com.rempawl.image.processing.core

import kotlinx.coroutines.flow.Flow

// todo core module
interface UseCase<Param, Result> {
    suspend fun call(param: Param): Result
}

interface FlowUseCase<Param, Result> {
    fun call(param: Param): Flow<Result>
}