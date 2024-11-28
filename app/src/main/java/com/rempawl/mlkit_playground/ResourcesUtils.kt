package com.rempawl.mlkit_playground

import android.content.res.Resources
import android.util.TypedValue

object ResourcesUtils {
    fun Resources.dpToPx() =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, displayMetrics)
}