package com.rempawl.mlkit_playground

data class ImageProcessingState(
    val detectedObjects: List<DetectedObject> = emptyList(),
    val detectedTextObjects: List<DetectedTextObject> = emptyList(),
    val showError: Boolean = false,
    val imageUri: String = "",
    val isProgressVisible: Boolean = false
)