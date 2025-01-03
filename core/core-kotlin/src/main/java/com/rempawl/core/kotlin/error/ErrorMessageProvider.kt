package com.rempawl.core.kotlin.error

interface ErrorMessageProvider {
    fun getErrorMessageFor(error: AppError): String
}