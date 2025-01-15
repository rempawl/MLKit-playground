package com.rempawl.image.processing.viewmodel

import com.rempawl.core.viewmodel.mvi.Action
import com.rempawl.core.ui.bottomsheet.ImageSourcePickerOption

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