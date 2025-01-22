package com.rempawl.image.processing.model

data class ImageProcessingResult(
    val detectedTextObjects: List<DetectedTextObject>,
    val detectedObjects: List<DetectedObject>,
    val imageWidth: Int,
    val imageHeight: Int,
)