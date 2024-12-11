package com.rempawl.image.processing

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
// todo make selaed class and add state.error
@Immutable
@Parcelize
data class ImageState(
    val height: Int = 0,
    val width: Int = 0,
    val uri: String = "",
) : Parcelable