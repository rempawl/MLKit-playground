package com.rempawl.mlkit_playground.ui

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap

fun ImageView.setupImageView() {
    adjustViewBounds = true
    scaleType = ImageView.ScaleType.CENTER_INSIDE
}

fun ImageView.copyBitmapFromDrawable(): Bitmap = drawable.toBitmap().run {
    val copy = copy(Bitmap.Config.ARGB_8888, true)
    recycle()
    copy
}
