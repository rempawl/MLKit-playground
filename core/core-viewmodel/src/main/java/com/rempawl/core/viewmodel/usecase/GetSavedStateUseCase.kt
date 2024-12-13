package com.rempawl.core.viewmodel.usecase

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.rempawl.core.android.ParcelableUtils
import com.rempawl.core.kotlin.DispatchersProvider
import com.rempawl.core.kotlin.UseCase
import kotlinx.coroutines.withContext

abstract class GetSavedStateUseCase<T>(
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