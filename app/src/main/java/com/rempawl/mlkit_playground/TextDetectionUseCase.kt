package com.rempawl.mlkit_playground

import android.util.Log
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TextDetectionUseCase(private val textRecognizer: TextRecognizer) :
    UseCase<InputImage, Result<List<DetectedTextObject>>> {

    override suspend fun call(param: InputImage): Result<List<DetectedTextObject>> =
        suspendCancellableCoroutine {
            textRecognizer.process(param)
                .addOnSuccessListener { vision ->
                    val textObjects = vision.textBlocks.filter { it.boundingBox != null }
                        .map {
                            DetectedTextObject(it.boundingBox!!.toRectF())
                        }
                    it.resume(Result.success(textObjects))
                }
                .addOnFailureListener { e ->
                    it.resumeWith(Result.failure(e))
                    Log.d("kruci", "error ${e.printStackTrace()}")
                }
        }
}