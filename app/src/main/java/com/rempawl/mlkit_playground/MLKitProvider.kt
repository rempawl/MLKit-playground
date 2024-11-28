package com.rempawl.mlkit_playground

import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object MLKitProvider {
    private val localModel by lazy {
        LocalModel.Builder()
            .setAssetFilePath("object_labeler.tflite")
            .build()
    }


    private val customObjectDetectorOptions by lazy {
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .setMaxPerObjectLabelCount(2)
            .setClassificationConfidenceThreshold(0.5f)
            .build()
    }

    fun provideObjectDetector(): ObjectDetector =
        ObjectDetection.getClient(customObjectDetectorOptions)

    fun provideTextRecognizer(): TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
}