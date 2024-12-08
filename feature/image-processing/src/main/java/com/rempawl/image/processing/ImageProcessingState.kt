package com.rempawl.image.processing

import androidx.compose.runtime.Immutable
import com.rempawl.image.processing.core.FilePickerOption

@Immutable
data class ImageProcessingState(
    val cameraUri: String = "",
    val detectedObjects: List<DetectedObject> = emptyList(),
    val detectedTextObjects: List<DetectedTextObject> = emptyList(),
    val showError: Boolean = false,
    val isProgressVisible: Boolean = false,
    val imageState: ImageState = ImageState(),
    val sourcePickerOptions: List<FilePickerOption> = emptyList(),
) {
    val isSourcePickerVisible: Boolean get() = sourcePickerOptions.isNotEmpty()
}

@Immutable
data class ImageState(
    val height: Int = 0,
    val width: Int = 0,
    val uri: String = "",
)
