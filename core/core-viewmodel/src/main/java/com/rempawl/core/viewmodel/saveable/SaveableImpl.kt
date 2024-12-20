package com.rempawl.core.viewmodel.saveable

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import arrow.core.Either
import com.rempawl.core.android.ParcelableUtils
import com.rempawl.core.kotlin.dispatcher.DispatchersProvider
import kotlinx.coroutines.withContext

/***
 *  Default implementation of the [Saveable] interface.
 *
 *  This class uses a  [SavedStateHandle] to store and retrieve state.
 *  It relies on [ParcelableUtils] to serialize and deserialize Parcelable objects.
 *  Operations are performed on the IO dispatcher provided by [DispatchersProvider].
 *
 */
internal class SaveableImpl(
    private val savedStateHandle: SavedStateHandle,
    private val parcelableUtils: ParcelableUtils,
    private val dispatchersProvider: DispatchersProvider,
) : Saveable {

    /**
     * Saves a Parcelable state to the SavedStateHandle.
     *
     * @param param The [Saveable.SaveStateParam] containing the key provider and state to save.
     * @param <T> The type of the Parcelable state.
     * prints Exception stacktrace If an error occurs during the save operation.
     */
    override suspend fun <T : Parcelable> saveState(param: Saveable.SaveStateParam<T>): Unit =
        withContext(dispatchersProvider.io) {
            Either.catch {
                savedStateHandle.setSavedStateProvider(param.keyProvider) {
                    bundleOf(param.keyState to param.state) // todo test
                }
            }.onLeft {
                it.printStackTrace()
            }
        }

    /**
     * Retrieves a saved state from the SavedStateHandle.
     *
     * @param param The [Saveable.GetSavedStateParam] containing the key provider and key state.
     * @param <T> The type of the saved state.
     * @return The saved state, or null if not found.
     * prints stacktrace of Exception If an error occurs during the retrieval operation.
     */
    override suspend fun <T> getState(param: Saveable.GetSavedStateParam): T? =
        withContext(dispatchersProvider.io) {
            Either.catch<T?> {
                savedStateHandle
                    .get<Bundle>(param.keyProvider)
                    ?.run { parcelableUtils.getParcelableFrom(this, param.keyState) }
            }
                .onLeft { it.printStackTrace() } // todo return either?
                .getOrNull()
            // todo test
        }
}