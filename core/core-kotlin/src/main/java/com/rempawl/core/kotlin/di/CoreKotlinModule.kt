package com.rempawl.core.kotlin.di

import com.rempawl.core.kotlin.dispatcher.DispatchersProviderImpl
import com.rempawl.core.kotlin.dispatcher.DispatchersProvider
import com.rempawl.core.kotlin.error.ErrorManager
import com.rempawl.core.kotlin.error.ErrorManagerImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val coreKotlinModule = module {
    factoryOf<DispatchersProvider>(::DispatchersProviderImpl)
    factoryOf<ErrorManager>(::ErrorManagerImpl)
}