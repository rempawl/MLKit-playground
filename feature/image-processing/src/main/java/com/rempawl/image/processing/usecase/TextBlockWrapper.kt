package com.rempawl.image.processing.usecase

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

data class TextBlockWrapper(val boundingBox: Rect? = null) {
    companion object {
        fun from(textBlock: Text.TextBlock) = TextBlockWrapper(textBlock.boundingBox)
    }
}