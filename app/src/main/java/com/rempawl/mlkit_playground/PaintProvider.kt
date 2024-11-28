package com.rempawl.mlkit_playground

import android.content.res.Resources
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

    fun getTextPaint(resources: Resources): Paint = customize {
        style = Paint.Style.FILL
        color = Color.CYAN
        textSize = resources.getDimension(R.dimen.font_size_object_detection)
    }
}