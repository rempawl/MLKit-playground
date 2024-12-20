package com.rempawl.core.kotlin.usecase

import com.rempawl.core.kotlin.extensions.EitherResult
import kotlinx.coroutines.flow.Flow

interface UseCase<Param, Result> {
    suspend fun call(param: Param): Result
}

interface ResultUseCase<Param, Result> {
    suspend fun call(param: Param): EitherResult<Result>
}

interface FlowUseCase<Param, Result> {
    fun call(param: Param): Flow<Result>
}

interface FlowResultUseCase<Param, Result> {
    fun call(param: Param): Flow<EitherResult<Result>>
}