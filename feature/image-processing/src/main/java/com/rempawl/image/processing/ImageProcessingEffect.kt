package com.rempawl.image.processing

import com.rempawl.image.processing.core.Effect
import com.rempawl.image.processing.core.GalleryPickerOption

sealed interface ImageProcessingEffect : Effect {
    data class TakePicture(val uri: String) : ImageProcessingEffect
    data class OpenGallery(val pickerOption: GalleryPickerOption) : ImageProcessingEffect
}