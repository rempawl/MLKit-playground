package com.rempawl.image.processing.repository

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.rempawl.core.kotlin.EitherResult
import com.rempawl.image.processing.usecase.TextBlockWrapper

interface MLKitDetectionRepository {
    suspend fun detectObjects(inputImage: InputImage): EitherResult<List<DetectedObject>>
    suspend fun detectText(inputImage: InputImage): EitherResult<List<TextBlockWrapper>>
}