package com.rempawl.image.processing

import com.rempawl.image.processing.core.Action
import com.rempawl.image.processing.core.FilePickerOption

sealed interface ImageProcessingAction : Action {
    data object SelectImageFabClicked : ImageProcessingAction
    data object HideImageSourcePicker : ImageProcessingAction

    data class PictureTaken(
        val isImageSaved: Boolean,
    ) : ImageProcessingAction

    data class GalleryImagePicked(
        val imageUri: String,
    ) : ImageProcessingAction

    data class FilePickerOptionSelected(val option: FilePickerOption) : ImageProcessingAction
}