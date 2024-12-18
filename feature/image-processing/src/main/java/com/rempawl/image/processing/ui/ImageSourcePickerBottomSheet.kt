package com.rempawl.image.processing.ui

import androidx.compose.runtime.Composable
import com.rempawl.bottomsheet.ImageSourcePickerBottomSheet
import com.rempawl.image.processing.viewmodel.ImageProcessingAction
import com.rempawl.image.processing.viewmodel.ImageProcessingAction.ImageSourcePickerOptionSelected
import com.rempawl.image.processing.viewmodel.ImageProcessingState

@Composable
fun ImageSourcePickerBottomSheet(
    submitAction: (ImageProcessingAction) -> Unit,
    state: ImageProcessingState,
) {
    ImageSourcePickerBottomSheet(
        pickerOptions = state.sourcePickerOptions,
        onPickerOptionSelected = { submitAction(ImageSourcePickerOptionSelected(it)) },
        onDismiss = { submitAction(ImageProcessingAction.HideImageSourcePicker) }
    )
}
