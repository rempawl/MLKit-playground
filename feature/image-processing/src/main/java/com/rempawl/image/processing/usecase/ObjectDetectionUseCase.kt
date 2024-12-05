package com.rempawl.image.processing.usecase

import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.DetectedObject
import com.rempawl.image.processing.core.EitherResult
import com.rempawl.image.processing.core.UseCase
import kotlin.math.roundToInt

class ObjectDetectionUseCase(private val mlKitDetectionRepository: MLKitDetectionRepository) :
    UseCase<InputImage, EitherResult<List<DetectedObject>>> {

    override suspend fun call(param: InputImage): EitherResult<List<DetectedObject>> =
        mlKitDetectionRepository.detectObjects(param)
            .map { objects ->
                objects.map {
                    DetectedObject(
                        rect = it.boundingBox.toRectF(),
                        labels = it.labels.joinToString(", ") { label ->
                            "${label.text} - ${(label.confidence * 100).roundToInt()}%"
                        }
                    )
                }
            }
}