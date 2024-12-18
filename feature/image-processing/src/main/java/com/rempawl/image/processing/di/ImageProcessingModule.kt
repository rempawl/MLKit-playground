package com.rempawl.image.processing.di

import com.rempawl.image.processing.viewmodel.ImageProcessingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val imageProcessingModule = module {
    includes(dataImageProcessingModule)
    viewModelOf(::ImageProcessingViewModel)
}