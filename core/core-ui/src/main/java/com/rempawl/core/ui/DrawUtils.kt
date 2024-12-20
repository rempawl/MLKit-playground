package com.rempawl.core.ui

import android.graphics.RectF
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.ContentDrawScope

fun ContentDrawScope.createScaleMatrix(height : Float, width : Float): Matrix {
    val widthRatio = size.width / width
    val heightRatio = size.height / height
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