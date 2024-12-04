package com.rempawl.image.processing.ui

import android.graphics.RectF
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import com.rempawl.image.processing.ImageState

fun ContentDrawScope.createScaleMatrix(imageState: ImageState): Matrix {
    val widthRatio = size.width / imageState.width.toFloat()
    val heightRatio = size.height / imageState.height.toFloat()
    return Matrix().apply {
        this.scale(
            x = widthRatio,
            y = heightRatio,
        )
    }
}

fun RectF.toComposeRect(): Rect = Rect(
    this.left,
    this.top,
    this.right,
    this.bottom
)