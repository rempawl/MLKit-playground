package com.rempawl.image.processing.di

import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.text.TextRecognizer
import com.rempawl.image.processing.repository.ImageProcessingRepository
import com.rempawl.image.processing.repository.ImageProcessingRepositoryImpl
import com.rempawl.image.processing.repository.MLKitDetectionRepository
import com.rempawl.image.processing.repository.MLKitDetectionRepositoryImpl
import com.rempawl.image.processing.usecase.GetCameraPhotoUriUseCase
import com.rempawl.image.processing.usecase.GetInputImageUseCase
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataImageProcessingModule = module {
    singleOf<TextRecognizer>(MLKitProvider::provideTextRecognizer)
    singleOf<ObjectDetector>(MLKitProvider::provideObjectDetector)
    single<ImageProcessingRepository> { ImageProcessingRepositoryImpl(get(), androidContext()) }
    single<MLKitDetectionRepository> { MLKitDetectionRepositoryImpl(get(), get()) }

    factoryOf(::ObjectDetectionUseCase)
    factoryOf(::TextDetectionUseCase)
    factoryOf(::GetInputImageUseCase)
    factoryOf(::GetCameraPhotoUriUseCase)
}