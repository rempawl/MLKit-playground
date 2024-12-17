package com.rempawl.image.processing.viewmodel

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.rempawl.image.processing.core.ImageSourcePickerOption
import com.rempawl.image.processing.model.DetectedObject
import com.rempawl.image.processing.model.DetectedTextObject
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class ImageProcessingState(
    val cameraUri: String = "",
    val detectedObjects: List<DetectedObject> = emptyList(),
    val detectedTextObjects: List<DetectedTextObject> = emptyList(),
    val showError: Boolean = false,
    val isProgressVisible: Boolean = false,
    val imageState: ImageState = ImageState(),
    val sourcePickerOptions: List<ImageSourcePickerOption> = emptyList(),
) : Parcelable {
    val isSourcePickerVisible: Boolean get() = sourcePickerOptions.isNotEmpty()
}
