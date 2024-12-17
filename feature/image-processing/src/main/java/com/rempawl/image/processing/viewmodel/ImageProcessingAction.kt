package com.rempawl.image.processing.viewmodel

import com.rempawl.image.processing.core.Action
import com.rempawl.image.processing.core.ImageSourcePickerOption

sealed interface ImageProcessingAction : Action {
    data object SelectImageFabClicked : ImageProcessingAction
    data object HideImageSourcePicker : ImageProcessingAction
    data object LifecycleStopped : ImageProcessingAction

    data class PictureTaken(
        val isImageSaved: Boolean,
    ) : ImageProcessingAction

    data class GalleryImagePicked(
        val imageUri: String,
    ) : ImageProcessingAction

    data class ImageSourcePickerOptionSelected(val option: ImageSourcePickerOption) :
        ImageProcessingAction
}