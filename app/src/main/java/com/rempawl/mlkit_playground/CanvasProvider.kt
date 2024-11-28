package com.rempawl.mlkit_playground

import android.graphics.Bitmap
import android.graphics.Canvas

class CanvasProvider() {
    private val canvas by lazy {
        Canvas()
    }

    fun use(bitmap: Bitmap, block: Canvas.() -> Unit) {
        canvas.setBitmap(bitmap)
        canvas.block()
        canvas.setBitmap(null)
    }
}