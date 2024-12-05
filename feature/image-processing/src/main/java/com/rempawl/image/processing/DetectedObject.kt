package com.rempawl.image.processing

import android.graphics.RectF

data class DetectedObject(
    val rect: RectF,
    val labels: String,
)