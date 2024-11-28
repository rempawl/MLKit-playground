package com.rempawl.image.processing

// todo core module
interface UseCase<Param, Result> {
    suspend fun call(param: Param): Result
}