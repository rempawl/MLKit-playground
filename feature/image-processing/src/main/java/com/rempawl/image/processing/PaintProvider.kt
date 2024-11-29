package com.rempawl.image.processing

import android.graphics.Color
import android.graphics.Paint

class PaintProvider {

    private val paint by lazy { Paint() }

    fun customize(decorator: Paint.() -> Unit): Paint {
        return paint.apply(decorator)
    }

    fun getObjectPaint(): Paint =
        customize {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.SQUARE
            strokeWidth = 5.0f
            color = Color.CYAN
        }

    fun getTextPaint(fontSize: Float): Paint = customize {
        style = Paint.Style.FILL
        color = Color.CYAN
        textSize = fontSize
    }
}