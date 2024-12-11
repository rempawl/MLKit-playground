package com.rempawl.image.processing.core

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

class ParcelableUtils {
    // todo core-android
    inline fun <reified T : Parcelable> getParcelableFrom(bundle: Bundle, key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, T::class.java)
        } else {
            bundle.getParcelable(key)
        }
}