package com.rempawl.image.processing.viewmodel

import com.rempawl.core.viewmodel.mvi.Effect
import com.rempawl.bottomsheet.GalleryPickerOption

sealed interface ImageProcessingEffect : Effect {
    data class TakePicture(val uri: String) : ImageProcessingEffect
    data class OpenGallery(val pickerOption: GalleryPickerOption) : ImageProcessingEffect
}