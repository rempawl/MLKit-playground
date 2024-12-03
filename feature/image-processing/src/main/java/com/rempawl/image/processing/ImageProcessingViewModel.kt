package com.rempawl.image.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.core.onError
import com.rempawl.image.processing.core.onSuccess
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageProcessingViewModel(
    private val objectDetectionUseCase: ObjectDetectionUseCase,
    private val textDetectionUseCase: TextDetectionUseCase,
) : ViewModel() {

    // todo kotlin 2.x explicit backing field
    private val _state = MutableStateFlow(ImageProcessingState())
    val state: StateFlow<ImageProcessingState> get() = _state.asStateFlow()

    fun processImage(inputImage: InputImage, imageUri: String) {
        _state.update { it.copy(isProgressVisible = true) }
        viewModelScope.launch {
            // todo lift logic to StateCase
            either {
                val texts = async { textDetectionUseCase.call(inputImage).bind() }
                val objects = async { objectDetectionUseCase.call(inputImage).bind() }
                texts.await() to objects.await()
            }
                .onSuccess { (texts, objects) ->
                    _state.update {
                        it.copy(
                            isProgressVisible = false,
                            imageUri = imageUri,
                            showError = false,
                            detectedTextObjects = texts,
                            detectedObjects = objects
                        )
                    }
                }
                .onError {
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
