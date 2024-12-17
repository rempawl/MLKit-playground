package com.rempawl.core.viewmodel.usecase

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import com.rempawl.core.kotlin.DispatchersProvider
import com.rempawl.core.kotlin.UseCase
import com.rempawl.core.viewmodel.usecase.SaveStateUseCase.Param
import kotlinx.coroutines.withContext

abstract class SaveStateUseCase<T : Parcelable>(
    private val savedStateHandle: SavedStateHandle,
    private val dispatchersProvider: DispatchersProvider,
) : UseCase<Param<T>, Unit> {

    data class Param<State>(
        val state: State,
        val keyState: String,
        val keyProvider: String,
    )

    override suspend fun call(param: Param<T>) =
        withContext(dispatchersProvider.io) {
            savedStateHandle.setSavedStateProvider(param.keyProvider) {
                bundleOf(param.keyState to param.state)
            }
        }
}