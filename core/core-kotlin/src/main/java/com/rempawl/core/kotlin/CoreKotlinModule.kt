package com.rempawl.core.kotlin

import DispatchersProviderImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val coreKotlinModule = module {
    factoryOf<DispatchersProvider>(::DispatchersProviderImpl)
}