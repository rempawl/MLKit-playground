package com.rempawl.core.viewmodel.saveable

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import com.rempawl.core.android.ParcelableUtils
import com.rempawl.core.kotlin.DispatchersProvider
import kotlinx.coroutines.withContext

internal class SaveableImpl(
    private val savedStateHandle: SavedStateHandle,
    private val parcelableUtils: ParcelableUtils,
    private val dispatchersProvider: DispatchersProvider,
) : Saveable {

    override suspend fun <T : Parcelable> saveState(param: Saveable.SaveStateParam<T>): Unit =
        withContext(dispatchersProvider.io) {
            savedStateHandle.setSavedStateProvider(param.keyProvider) {
                bundleOf(param.keyState to param.state) // todo test
            }
        }

    override suspend fun <T> getState(param: Saveable.GetSavedStateParam): T? =
        withContext(dispatchersProvider.io) {
            savedStateHandle
                .get<Bundle>(param.keyProvider)
                ?.run { parcelableUtils.getParcelableFrom(this, param.keyState) }
            // todo test
        }
}