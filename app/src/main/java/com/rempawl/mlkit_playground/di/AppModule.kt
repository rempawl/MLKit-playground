package com.rempawl.mlkit_playground.di

import com.rempawl.core.android.coreAndroidModule
import com.rempawl.core.kotlin.di.coreKotlinModule
import com.rempawl.core.viewmodel.di.coreViewModelModule
import com.rempawl.image.processing.di.imageProcessingModule
import org.koin.dsl.module

val appModule = module {
    includes(imageProcessingModule, coreAndroidModule, coreKotlinModule, coreViewModelModule)
}