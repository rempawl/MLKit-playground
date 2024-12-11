package com.rempawl.image.processing.di

import androidx.lifecycle.SavedStateHandle
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.text.TextRecognizer
import com.rempawl.image.processing.ImageProcessingState
import com.rempawl.image.processing.ImageProcessingViewModel
import com.rempawl.image.processing.core.DispatchersProvider
import com.rempawl.image.processing.core.DispatchersProviderImpl
import com.rempawl.image.processing.core.FileUtils
import com.rempawl.image.processing.core.ParcelableUtils
import com.rempawl.image.processing.usecase.GetSavedStateUseCase
import com.rempawl.image.processing.usecase.MLKitDetectionRepository
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.SaveStateUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val imageProcessingModule = module {
    single { FileUtils(get(), androidContext()) }
    singleOf<DispatchersProvider>(::DispatchersProviderImpl)
    singleOf<TextRecognizer>(MLKitProvider::provideTextRecognizer)
    singleOf<ObjectDetector>(MLKitProvider::provideObjectDetector)
    factoryOf(::ParcelableUtils)
    factoryOf(::ObjectDetectionUseCase)
    factoryOf(::TextDetectionUseCase)
    factoryOf(::MLKitDetectionRepository)
    factory { (savedStateHandle: SavedStateHandle) ->
        SaveStateUseCase<ImageProcessingState>(savedStateHandle = savedStateHandle, get())
    }
    factory { (savedStateHandle: SavedStateHandle) ->
        GetSavedStateUseCase<ImageProcessingState>(
            savedStateHandle = savedStateHandle,
            get(),
            get()
        )
    }
    viewModel { (savedStateHandle: SavedStateHandle) ->
        val savedStateUseCase by inject<SaveStateUseCase<ImageProcessingState>> {
            parametersOf(savedStateHandle)
        }
        val getSavedStateUseCase by inject<GetSavedStateUseCase<ImageProcessingState>> {
            parametersOf(savedStateHandle)
        }
        ImageProcessingViewModel(
            objectDetectionUseCase = get(),
            textDetectionUseCase = get(),
            fileUtils = get(),
            saveStateUseCase = savedStateUseCase,
            getSavedStateUseCase = getSavedStateUseCase,
        )
    }
}