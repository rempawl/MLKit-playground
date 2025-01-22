package com.rempawl.image.processing.usecase

import arrow.core.flatMap
import arrow.core.raise.either
import com.rempawl.core.kotlin.extensions.EitherResult
import com.rempawl.core.kotlin.usecase.ResultUseCase
import com.rempawl.image.processing.model.ImageProcessingResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Use case for processing an image.
 *
 * This use case orchestrates the process of detecting text and objects within an image.
 * It utilizes other use cases for text detection, object detection, and retrieving the input image.
 * The result is an `ImageProcessingResult` containing the detected elements and image dimensions.
 */
class ProcessImageUseCase internal constructor(
    private val textDetectionUseCase: TextDetectionUseCase,
    private val objectDetectionUseCase: ObjectDetectionUseCase,
    private val getInputImageUseCase: GetInputImageUseCase,
) : ResultUseCase<String, ImageProcessingResult> {

    override suspend fun call(param: String): EitherResult<ImageProcessingResult> = coroutineScope {
        getInputImageUseCase.call(param).flatMap { inputImage ->
            either {
                val texts = async { textDetectionUseCase.call(inputImage).bind() }
                val objects = async { objectDetectionUseCase.call(inputImage).bind() }
                ImageProcessingResult(
                    detectedTextObjects = texts.await(),
                    detectedObjects = objects.await(),
                    imageWidth = inputImage.width,
                    imageHeight = inputImage.height
                )
            }
        }
    }
}
