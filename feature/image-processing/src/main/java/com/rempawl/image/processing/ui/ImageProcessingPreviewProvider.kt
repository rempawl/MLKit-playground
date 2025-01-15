package com.rempawl.image.processing.ui

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.rempawl.image.processing.viewmodel.ImageProcessingState
import com.rempawl.image.processing.viewmodel.ImageState

class ImageProcessingPreviewProvider : CollectionPreviewParameterProvider<ImageProcessingState>(
    listOf(
        ImageProcessingState(),
        ImageProcessingState(
            imageState = ImageState(
                1024, 1024, "uri"
            )
        ),
    )
)