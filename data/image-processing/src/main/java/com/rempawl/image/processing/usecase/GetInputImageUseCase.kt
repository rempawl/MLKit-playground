package com.rempawl.image.processing.usecase

import com.google.mlkit.vision.common.InputImage
import com.rempawl.core.kotlin.EitherResult
import com.rempawl.core.kotlin.ResultUseCase
import com.rempawl.image.processing.repository.ImageProcessingRepository

internal class GetInputImageUseCase(private val repository: ImageProcessingRepository) :
    ResultUseCase<String, InputImage> {
    override suspend fun call(param: String): EitherResult<InputImage> =
        repository.getInputImage(param)
}