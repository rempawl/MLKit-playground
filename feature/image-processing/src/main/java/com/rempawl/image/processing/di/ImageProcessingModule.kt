package com.rempawl.image.processing.di

import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.text.TextRecognizer
import com.rempawl.core.android.ParcelableUtils
import com.rempawl.image.processing.ImageProcessingRepository
import com.rempawl.image.processing.ImageProcessingState
import com.rempawl.image.processing.ImageProcessingViewModel
import com.rempawl.image.processing.usecase.GetSavedStateUseCase
import com.rempawl.image.processing.usecase.MLKitDetectionRepository
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.SaveStateUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val imageProcessingModule = module {
    single { ImageProcessingRepository(get(), androidContext()) }
    singleOf<TextRecognizer>(MLKitProvider::provideTextRecognizer)
    singleOf<ObjectDetector>(MLKitProvider::provideObjectDetector)
    factoryOf(::ParcelableUtils)
    factoryOf(::ObjectDetectionUseCase)
    factoryOf(::TextDetectionUseCase)
    factoryOf(::MLKitDetectionRepository)
    factory {
        SaveStateUseCase<ImageProcessingState>(
            get(), get()
        )
    } // todo subclassing for easier DI
    factory { GetSavedStateUseCase<ImageProcessingState>(get(), get(), get()) }
    viewModelOf(::ImageProcessingViewModel)
}