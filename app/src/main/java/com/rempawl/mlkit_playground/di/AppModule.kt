package com.rempawl.mlkit_playground.di

import com.rempawl.image.processing.di.imageProcessingModule
import org.koin.dsl.module

val appModule = module {
    includes(imageProcessingModule)
}