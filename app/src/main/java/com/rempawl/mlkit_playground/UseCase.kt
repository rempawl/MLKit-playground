package com.rempawl.mlkit_playground

interface UseCase<Param, Result> {
    suspend fun call(param: Param): Result
}