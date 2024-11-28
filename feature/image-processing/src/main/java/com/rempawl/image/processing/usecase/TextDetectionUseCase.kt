package com.rempawl.image.processing.usecase

import android.util.Log
import androidx.core.graphics.toRectF
import arrow.core.left
import arrow.core.right
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.rempawl.image.processing.DetectedTextObject
import com.rempawl.image.processing.core.EitherResult
import com.rempawl.image.processing.core.UseCase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TextDetectionUseCase(private val textRecognizer: TextRecognizer) :
    UseCase<InputImage, EitherResult<List<DetectedTextObject>>> {

    override suspend fun call(param: InputImage): EitherResult<List<DetectedTextObject>> =
        suspendCancellableCoroutine {
            textRecognizer.process(param).addOnSuccessListener { vision ->
                val textObjects = vision.textBlocks.filter { it.boundingBox != null }.map {
                    DetectedTextObject(it.boundingBox!!.toRectF())
                }
                it.resume(textObjects.right())
            }.addOnFailureListener { e ->
                it.resume(e.left())
                Log.d("kruci", "error ${e.printStackTrace()}")
            }
        }
}