package com.rempawl.image.processing

import android.content.Context
import com.rempawl.core.android.DefaultErrorMessageProvider
import com.rempawl.core.kotlin.error.DefaultError
import com.rempawl.image.processing.error.ImageNotSavedError
import com.rempawl.image.processing.error.ImageProcessingErrorMessageProvider
import com.rempawl.test.utils.verifyNever
import com.rempawl.test.utils.verifyOnce
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ImageProcessingErrorMessageProviderTest {

    private lateinit var imageProcessingErrorMessageProvider: ImageProcessingErrorMessageProvider
    private val context = mockk<Context>(relaxed = true)
    private val defaultErrorMessageProvider = mockk<DefaultErrorMessageProvider>(relaxed = true)

    @BeforeEach
    fun setup() {
        imageProcessingErrorMessageProvider =
            ImageProcessingErrorMessageProvider(context, defaultErrorMessageProvider)
    }

    @Test
    fun `when get error message for ImageNotSaved error called, then correct string retrieved`() {
        verifyNever { context.getString(R.string.error_image_not_saved) }

        imageProcessingErrorMessageProvider.getErrorMessageFor(ImageNotSavedError)

        verifyOnce {
            context.getString(R.string.error_image_not_saved)
        }
    }

    @Test
    fun `when error type not found, then default error message retrieved`() {
        verifyNever { defaultErrorMessageProvider.getErrorMessageFor(any()) }

        imageProcessingErrorMessageProvider.getErrorMessageFor(DefaultError())

        verifyOnce { defaultErrorMessageProvider.getErrorMessageFor(any()) }
        verifyNever { context.getString(any()) }
    }
}