package com.rempawl.image.processing.usecase

import android.util.Log
import androidx.core.graphics.toRectF
import arrow.core.left
import arrow.core.right
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetector
import com.rempawl.image.processing.DetectedObject
import com.rempawl.image.processing.core.EitherResult
import com.rempawl.image.processing.core.UseCase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.roundToInt

class ObjectDetectionUseCase(private val objectDetector: ObjectDetector) :
    UseCase<InputImage, EitherResult<List<DetectedObject>>> {

    override suspend fun call(param: InputImage): EitherResult<List<DetectedObject>> =
        suspendCancellableCoroutine { continuation ->
            objectDetector.process(param)
                .addOnSuccessListener { objects ->
                    val detectedObjects = objects.map {
                        // todo test
                        DetectedObject(
                            rect = it.boundingBox.toRectF(),
                            labels = it.labels.joinToString(", ") { it.text + "- ${(it.confidence * 100).roundToInt()}%" }
                        )
                    }
                    continuation.resume(detectedObjects.right())
                }
                .addOnFailureListener { e ->
                    continuation.resume(e.left())
                    Log.d("kruci", "error ${e.printStackTrace()}")
                }

        }
}