package com.rempawl.core.ui.bottomsheet

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.rempawl.core.ui.bottomsheet.GalleryPickerOption.IMAGE_ONLY

enum class GalleryPickerOption {
    IMAGE_ONLY;
}

fun GalleryPickerOption.toPickVisualMediaRequest(): PickVisualMediaRequest =
    when (this) {
        IMAGE_ONLY -> PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    }