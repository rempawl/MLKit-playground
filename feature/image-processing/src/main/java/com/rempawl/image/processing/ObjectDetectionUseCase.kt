package com.rempawl.image.processing

import android.util.Log
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetector
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.roundToInt

class ObjectDetectionUseCase(private val objectDetector: ObjectDetector) :
    UseCase<InputImage, Result<List<DetectedObject>>> {

    override suspend fun call(param: InputImage): Result<List<DetectedObject>> =
        suspendCancellableCoroutine { continuation ->
            objectDetector.process(param)
                .addOnSuccessListener { objects ->
                    val detectedObjects = objects.map {
                        DetectedObject(
                            rect = it.boundingBox.toRectF(),
                            labels = it.labels.joinToString(", ") { it.text + "- ${(it.confidence * 100).roundToInt()}%" }
                        )
                    }
                    continuation.resume(Result.success(detectedObjects))
                }
                .addOnFailureListener { e ->
                    continuation.resumeWith(Result.failure(e))
                    Log.d("kruci", "error ${e.printStackTrace()}")
                }

        }
}