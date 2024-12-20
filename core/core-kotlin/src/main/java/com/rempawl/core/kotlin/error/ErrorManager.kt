package com.rempawl.core.kotlin.error

import arrow.core.Either
import kotlinx.coroutines.flow.Flow


interface ErrorManager {
    val errors: Flow<Either<Unit, AppError>>
    suspend fun addError(error: UIError)
    suspend fun removeCurrentError()

    companion object {
        const val SIZE_ERROR_QUEUE = 3
    }
}