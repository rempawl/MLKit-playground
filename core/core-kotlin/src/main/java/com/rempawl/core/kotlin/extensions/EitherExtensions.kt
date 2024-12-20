package com.rempawl.core.kotlin.extensions

import arrow.core.Either

typealias EitherResult<T> = Either<Throwable, T>

inline fun <T> EitherResult<T>.onSuccess(block: (T) -> Unit): EitherResult<T> =
    onRight(block)

inline fun <T> EitherResult<T>.onError(block: (Throwable) -> Unit): EitherResult<T> =
    onLeft(block)
