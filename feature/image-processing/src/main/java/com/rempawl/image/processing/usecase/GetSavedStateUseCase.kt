package com.rempawl.image.processing.usecase

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.rempawl.image.processing.core.DispatchersProvider
import com.rempawl.image.processing.core.ParcelableUtils
import com.rempawl.image.processing.core.UseCase
import kotlinx.coroutines.withContext

// todo core-android module
class GetSavedStateUseCase<T>(
    private val savedStateHandle: SavedStateHandle,
    private val parcelableUtils: ParcelableUtils,
    private val dispatchersProvider: DispatchersProvider,
) : UseCase<GetSavedStateUseCase.SavedStateParam, T?> {
    data class SavedStateParam(
        val keyProvider: String,
        val keyState: String,
    )

    override suspend fun call(param: SavedStateParam): T? = withContext(dispatchersProvider.io) {
        savedStateHandle
            .get<Bundle>(param.keyProvider)
            ?.run { parcelableUtils.getParcelableFrom(this, param.keyState) }
    }
}