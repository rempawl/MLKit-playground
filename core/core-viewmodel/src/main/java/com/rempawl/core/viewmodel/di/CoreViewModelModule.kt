package com.rempawl.core.viewmodel.di

import com.rempawl.core.viewmodel.saveable.Saveable
import com.rempawl.core.viewmodel.saveable.SaveableImpl
import org.koin.dsl.module

val coreViewModelModule = module {
    factory<Saveable> {
        SaveableImpl(
            savedStateHandle = get(),
            parcelableUtils = get(),
            dispatchersProvider = get()
        )
    }
}