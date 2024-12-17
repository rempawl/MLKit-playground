package com.rempawl.core.android

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val coreAndroidModule = module {
    factoryOf(::ParcelableUtils)
}