package com.rempawl.image.processing.usecase

import arrow.core.left
import arrow.core.right
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.text.TextRecognizer
import com.rempawl.image.processing.core.EitherResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// todo data module
class MLKitDetectionRepository(
    private val objectDetector: ObjectDetector,
    private val textRecognizer: TextRecognizer,
) {

    suspend fun detectObjects(inputImage: InputImage): EitherResult<List<DetectedObject>> =
        suspendCancellableCoroutine { continuation ->
            objectDetector.process(inputImage)
                .addOnSuccessListener { objects -> continuation.resume(objects.right()) }
                .addOnFailureListener { e -> continuation.resume(e.left()) }
        }

    suspend fun detectText(inputImage: InputImage): EitherResult<List<TextBlockWrapper>> =
        suspendCancellableCoroutine {
            textRecognizer.process(inputImage)
                .addOnSuccessListener { vision ->
                    it.resume(vision.textBlocks.map { TextBlockWrapper.from(it) }.right())
                }.addOnFailureListener { e ->
                    it.resume(e.left())
                }
        }
}