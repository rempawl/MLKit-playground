package com.rempawl.core.ui

import android.graphics.RectF
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.ContentDrawScope

/**
 * Creates a scale matrix to fit content within the ContentDrawScope's bounds.
 *
 * This function calculates the scaling factors needed to fit content of a given width and height
 * within the available drawing area of the ContentDrawScope. It returns a Matrix object
 * that can be applied to scale the content accordingly.
 *
 * @param height The height of the content to be scaled.
 * @param width The width of the content to be scaled.
 * @return A Matrix object representing the calculated scale transformation.
 */
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