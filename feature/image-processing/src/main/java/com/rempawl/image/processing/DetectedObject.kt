package com.rempawl.image.processing

import android.graphics.RectF

data class DetectedObject(
    val rect: RectF,
    val labels: String,
) {
    val startX: Float get() = rect.left
    val startY get() = rect.top
}