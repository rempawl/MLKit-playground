package com.rempawl.image.processing.di

import com.rempawl.image.processing.usecase.GetImageProcessingSavedStateUseCase
import com.rempawl.image.processing.usecase.SaveImageProcessingStateUseCase
import com.rempawl.image.processing.viewmodel.ImageProcessingViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val imageProcessingModule = module {
    includes(dataImageProcessingModule)
    factoryOf(::SaveImageProcessingStateUseCase)
    factoryOf(::GetImageProcessingSavedStateUseCase)
    viewModelOf(::ImageProcessingViewModel)
}