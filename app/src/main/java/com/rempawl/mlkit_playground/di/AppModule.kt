package com.rempawl.mlkit_playground.di

import com.rempawl.core.android.coreAndroidModule
import com.rempawl.core.kotlin.coreKotlinModule
import com.rempawl.image.processing.di.imageProcessingModule
import org.koin.dsl.module

val appModule = module {
    includes(imageProcessingModule, coreAndroidModule, coreKotlinModule)
}