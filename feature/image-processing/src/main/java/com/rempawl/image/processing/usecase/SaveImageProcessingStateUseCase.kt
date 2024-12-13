package com.rempawl.image.processing.usecase

import androidx.lifecycle.SavedStateHandle
import com.rempawl.core.kotlin.DispatchersProvider
import com.rempawl.core.viewmodel.usecase.SaveStateUseCase
import com.rempawl.image.processing.viewmodel.ImageProcessingState

class SaveImageProcessingStateUseCase internal constructor(
    savedStateHandle: SavedStateHandle,
    dispatchersProvider: DispatchersProvider,
) : SaveStateUseCase<ImageProcessingState>(
    savedStateHandle,
    dispatchersProvider
)