package com.rempawl.image.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either.Companion.zipOrAccumulate
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageProcessingViewModel(
    private val objectDetectionUseCase: ObjectDetectionUseCase,
    private val textDetectionUseCase: TextDetectionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ImageProcessingState())

    // todo kotlin 2.0 backing field
    val state: StateFlow<ImageProcessingState> get() = _state.asStateFlow()

    fun processImage(inputImage: InputImage, imageUri: String) {
        _state.update { it.copy(isProgressVisible = true) }
        viewModelScope.launch {
            zipOrAccumulate(
                textDetectionUseCase.call(inputImage),
                objectDetectionUseCase.call(inputImage)
            ) { text, objects ->
                text to objects
            }.onRight { (texts, objects) ->
                _state.update {
                    it.copy(
                        isProgressVisible = false,
                        imageUri = imageUri,
                        showError = false,
                        detectedTextObjects = texts,
                        detectedObjects = objects
                    )
                }
            }.onLeft {
                _state.update {
                    it.copy(
                        isProgressVisible = false,
                        imageUri = "",
                        showError = true,
                        detectedTextObjects = emptyList(),
                        detectedObjects = emptyList()
                    )
                }
            }
        }
    }
}
