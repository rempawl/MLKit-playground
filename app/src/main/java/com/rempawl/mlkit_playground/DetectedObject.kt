package com.rempawl.mlkit_playground

import android.graphics.RectF

data class DetectedObject(
    val rect: RectF,
    val labels: String,
) {
    val startX: Float get() = rect.left
    val startY get() = rect.top
}