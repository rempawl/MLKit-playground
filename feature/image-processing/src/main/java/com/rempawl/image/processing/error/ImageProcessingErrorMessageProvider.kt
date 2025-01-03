package com.rempawl.image.processing.error

import android.content.Context
import com.rempawl.core.android.DefaultErrorMessageProvider
import com.rempawl.core.kotlin.error.AppError
import com.rempawl.core.kotlin.error.ErrorMessageProvider
import com.rempawl.image.processing.R

class ImageProcessingErrorMessageProvider internal constructor(
    private val context: Context,
    private val errorMessageProvider: DefaultErrorMessageProvider
) : ErrorMessageProvider {

    override fun getErrorMessageFor(error: AppError): String =
        when (error) {
            ImageNotSavedError -> context.getString(R.string.error_image_not_saved)
            else -> errorMessageProvider.getErrorMessageFor(error)
        }
}