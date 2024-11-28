package com.rempawl.mlkit_playground

import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.text.TextRecognizer
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<ObjectDetector>() { MLKitProvider.provideObjectDetector() }
    single<TextRecognizer>() { MLKitProvider.provideTextRecognizer() }
    factory<ObjectDetectionUseCase>() { ObjectDetectionUseCase(get()) }
    factory<TextDetectionUseCase>() { TextDetectionUseCase(get()) }
    viewModelOf(::ImageProcessingViewModel)
}