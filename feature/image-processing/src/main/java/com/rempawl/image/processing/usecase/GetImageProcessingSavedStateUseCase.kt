package com.rempawl.image.processing.usecase

import androidx.lifecycle.SavedStateHandle
import com.rempawl.core.android.ParcelableUtils
import com.rempawl.core.kotlin.DispatchersProvider
import com.rempawl.core.viewmodel.usecase.GetSavedStateUseCase
import com.rempawl.image.processing.viewmodel.ImageProcessingState

class GetImageProcessingSavedStateUseCase internal constructor(
    savedStateHandle: SavedStateHandle,
    parcelableUtils: ParcelableUtils,
    dispatchersProvider: DispatchersProvider,
) : GetSavedStateUseCase<ImageProcessingState>(
    savedStateHandle = savedStateHandle,
    parcelableUtils = parcelableUtils,
    dispatchersProvider = dispatchersProvider
)