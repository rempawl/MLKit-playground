package com.rempawl.image.processing

import androidx.compose.runtime.Immutable

@Immutable
data class ImageProcessingState(
    val detectedObjects: List<DetectedObject> = emptyList(),
    val detectedTextObjects: List<DetectedTextObject> = emptyList(),
    val showError: Boolean = false,
    val isProgressVisible: Boolean = false,
    val imageState: ImageState = ImageState(),
)

@Immutable
data class ImageState(
    val height: Int = 0,
    val width: Int = 0,
    val uri: String = "",
)
