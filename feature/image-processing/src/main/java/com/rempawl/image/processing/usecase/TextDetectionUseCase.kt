package com.rempawl.image.processing.usecase

import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.DetectedTextObject
import com.rempawl.image.processing.core.EitherResult
import com.rempawl.image.processing.core.UseCase

class TextDetectionUseCase(
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