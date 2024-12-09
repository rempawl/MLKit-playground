package com.rempawl.image.processing

import android.graphics.RectF
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetectedObject(
    val rect: RectF,
    val labels: String,
) : Parcelable