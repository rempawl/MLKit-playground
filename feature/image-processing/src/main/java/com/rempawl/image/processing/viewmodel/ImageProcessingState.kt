package com.rempawl.image.processing.viewmodel

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.rempawl.bottomsheet.ImageSourcePickerOption
import com.rempawl.core.kotlin.error.UIError
import com.rempawl.image.processing.model.DetectedObject
import com.rempawl.image.processing.model.DetectedTextObject
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class ImageProcessingState(
    val cameraUri: String = "",
    val detectedObjects: List<DetectedObject> = emptyList(),
    val detectedTextObjects: List<DetectedTextObject> = emptyList(),
    val error: UIError? = null,
    val isProgressVisible: Boolean = false,
    val imageState: ImageState = ImageState(),
    val sourcePickerOptions: List<ImageSourcePickerOption> = emptyList(),
) : Parcelable {
    val isSourcePickerVisible: Boolean get() = sourcePickerOptions.isNotEmpty()
}
