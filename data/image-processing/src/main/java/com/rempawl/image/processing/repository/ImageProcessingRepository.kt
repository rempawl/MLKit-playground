package com.rempawl.image.processing.repository

import com.google.mlkit.vision.common.InputImage
import com.rempawl.core.kotlin.EitherResult

interface ImageProcessingRepository {
    suspend fun getTmpCameraFileUriString(): EitherResult<String>
    suspend fun getInputImage(uri: String): EitherResult<InputImage>
}