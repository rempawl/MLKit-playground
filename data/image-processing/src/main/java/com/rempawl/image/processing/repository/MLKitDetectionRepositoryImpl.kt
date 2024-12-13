package com.rempawl.image.processing.repository

import arrow.core.left
import arrow.core.right
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.text.TextRecognizer
import com.rempawl.core.kotlin.EitherResult
import com.rempawl.image.processing.usecase.TextBlockWrapper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class MLKitDetectionRepositoryImpl(
    private val objectDetector: ObjectDetector,
    private val textRecognizer: TextRecognizer,
) : MLKitDetectionRepository {

    override suspend fun detectObjects(inputImage: InputImage): EitherResult<List<DetectedObject>> =
        suspendCancellableCoroutine { continuation ->
            objectDetector.process(inputImage)
                .addOnSuccessListener { objects -> continuation.resume(objects.right()) }
                .addOnFailureListener { e -> continuation.resume(e.left()) }
        }

    override suspend fun detectText(inputImage: InputImage): EitherResult<List<TextBlockWrapper>> =
        suspendCancellableCoroutine {
            textRecognizer.process(inputImage).addOnSuccessListener { vision ->
                it.resume(vision.textBlocks.map { TextBlockWrapper.from(it) }.right())
            }.addOnFailureListener { e ->
                it.resume(e.left())
            }
        }
}