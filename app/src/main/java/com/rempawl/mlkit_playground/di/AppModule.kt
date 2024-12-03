package com.rempawl.mlkit_playground.di

import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.text.TextRecognizer
import com.rempawl.image.processing.CanvasProvider
import com.rempawl.image.processing.ImageProcessingViewModel
import com.rempawl.image.processing.PaintProvider
import com.rempawl.image.processing.core.DispatchersProvider
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import com.rempawl.mlkit_playground.core.DispatchersProviderImpl
import com.rempawl.mlkit_playground.di.MLKitProvider.provideObjectDetector
import com.rempawl.mlkit_playground.di.MLKitProvider.provideTextRecognizer
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::CanvasProvider)
    singleOf(::PaintProvider)
    singleOf<DispatchersProvider>(::DispatchersProviderImpl)
    singleOf<ObjectDetector>(::provideObjectDetector)
    singleOf<TextRecognizer>(::provideTextRecognizer)
    factoryOf(::ObjectDetectionUseCase)
    factoryOf(::TextDetectionUseCase)
    viewModelOf(::ImageProcessingViewModel)
}