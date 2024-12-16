package com.rempawl.image.processing.usecase

import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.rempawl.core.kotlin.EitherResult
import com.rempawl.core.kotlin.UseCase
import com.rempawl.image.processing.model.DetectedObject
import com.rempawl.image.processing.repository.MLKitDetectionRepository
import kotlin.math.roundToInt

internal class ObjectDetectionUseCase(private val mlKitDetectionRepository: MLKitDetectionRepository) :
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