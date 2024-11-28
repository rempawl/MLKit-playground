package com.rempawl.mlkit_playground

import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.text.TextRecognizer
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    factoryOf(::CanvasProvider)
    factoryOf(::PaintProvider)
    single<ObjectDetector>() { MLKitProvider.provideObjectDetector() }
    single<TextRecognizer>() { MLKitProvider.provideTextRecognizer() }
    factoryOf(::ObjectDetectionUseCase)
    factoryOf(::TextDetectionUseCase)
    viewModelOf(::ImageProcessingViewModel)
}