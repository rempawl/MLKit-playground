package com.rempawl.core.android

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val coreAndroidModule = module {
    factoryOf(::ParcelableUtils)
    single { DefaultErrorMessageProvider(androidContext()) }
}
