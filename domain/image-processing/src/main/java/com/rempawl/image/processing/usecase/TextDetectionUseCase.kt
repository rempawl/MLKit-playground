package com.rempawl.image.processing.usecase

import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.rempawl.core.kotlin.extensions.EitherResult
import com.rempawl.core.kotlin.usecase.UseCase
import com.rempawl.image.processing.model.DetectedTextObject
import com.rempawl.image.processing.repository.MLKitDetectionRepository

internal class TextDetectionUseCase(
    private val mlKitDetectionRepository: MLKitDetectionRepository,
) : UseCase<InputImage, EitherResult<List<DetectedTextObject>>> {

    override suspend fun call(param: InputImage): EitherResult<List<DetectedTextObject>> =
        mlKitDetectionRepository.detectText(param)
            .map { textBlocks ->
                textBlocks
                    .filter { it.boundingBox != null }
                    .map { DetectedTextObject(it.boundingBox!!.toRectF()) }
            }
}