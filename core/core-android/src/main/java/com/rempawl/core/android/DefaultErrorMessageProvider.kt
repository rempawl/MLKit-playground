package com.rempawl.core.android

import android.content.Context
import com.rempawl.core.kotlin.error.AppError
import com.rempawl.core.kotlin.error.DefaultError
import com.rempawl.core.kotlin.error.ErrorMessageProvider

class DefaultErrorMessageProvider(private val context: Context) : ErrorMessageProvider {

    // todo checking throwable for defaultError
    override fun getErrorMessageFor(error: AppError): String = when (error) {
        is DefaultError -> context.getString(R.string.error_default_message)
        else -> context.getString(R.string.error_default_message)
    }
}