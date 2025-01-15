package com.rempawl.image.processing.model

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

internal data class TextBlockWrapper(val boundingBox: Rect? = null) {
    companion object {
        fun from(textBlock: Text.TextBlock) = TextBlockWrapper(textBlock.boundingBox)
    }
}