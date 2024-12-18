package com.rempawl.core.viewmodel.saveable

import android.os.Parcelable

interface Saveable {
    data class GetSavedStateParam(
        val keyProvider: String,
        val keyState: String,
    )

    data class SaveStateParam<State>(
        val state: State,
        val keyState: String,
        val keyProvider: String,
    )

    suspend fun <T : Parcelable> saveState(param: SaveStateParam<T>)
    suspend fun <T> getState(param: GetSavedStateParam): T?
}
