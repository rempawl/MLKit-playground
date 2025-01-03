package com.rempawl.core.kotlin.error

open class DefaultError(val throwable: Throwable? = null) : AppError {
    override fun toString(): String {
        return "${this.javaClass.simpleName} (throwable=$throwable). Message ${throwable?.message}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultError
        return throwable == other.throwable
    }

    override fun hashCode(): Int = (throwable?.hashCode() ?: 0) * 31
}
