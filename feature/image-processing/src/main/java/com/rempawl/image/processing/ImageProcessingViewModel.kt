package com.rempawl.image.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.core.DispatchersProvider
import com.rempawl.image.processing.core.onError
import com.rempawl.image.processing.core.onSuccess
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

// todo put viewmodel in other module
class ImageProcessingViewModel(
    private val objectDetectionUseCase: ObjectDetectionUseCase,
    private val textDetectionUseCase: TextDetectionUseCase,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ImageProcessingState())

    // todo kotlin 2.0 backing field
    val state: StateFlow<ImageProcessingState> get() = _state.asStateFlow()

    fun processImage(inputImage: InputImage, imageUri: String) {
        _state.update { it.copy(imageUri = imageUri, isProgressVisible = true) }
        viewModelScope.launch {
            supervisorScope {
                launchObjectDetection(inputImage)
                launchTextDetection(inputImage)
            }
            _state.update { it.copy(isProgressVisible = false) }
        }
    }

    private fun CoroutineScope.launchTextDetection(inputImage: InputImage) {
        launch(dispatchersProvider.default) {
            textDetectionUseCase.call(inputImage)
                .onSuccess { objects ->
                    _state.update {
                        it.copy(
                            detectedTextObjects = objects,
                            showError = false
                        )
                    }
                }
                .onError {
                    _state.update {
                        it.copy(
                            detectedTextObjects = emptyList(),
                            showError = true
                        )
                    }
                }
        }
    }

    private fun CoroutineScope.launchObjectDetection(inputImage: InputImage) {
        launch(dispatchersProvider.default) {
            objectDetectionUseCase.call(inputImage)
                .onSuccess { detectedObjects ->
                    _state.update {
                        it.copy(
                            detectedObjects = detectedObjects,
                            showError = false
                        )
                    }
                }
                .onError {
                    _state.update {
                        it.copy(
                            detectedObjects = emptyList(),
                            showError = true
                        )
                    }
                }
        }
    }
}
