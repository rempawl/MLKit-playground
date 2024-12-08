package com.rempawl.image.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.core.EitherResult
import com.rempawl.image.processing.core.FilePickerOption
import com.rempawl.image.processing.core.FileUtils
import com.rempawl.image.processing.core.GalleryPickerOption
import com.rempawl.image.processing.core.onError
import com.rempawl.image.processing.core.onSuccess
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageProcessingViewModel(
    private val objectDetectionUseCase: ObjectDetectionUseCase,
    private val textDetectionUseCase: TextDetectionUseCase,
    private val fileUtils: FileUtils,
) : ViewModel() {

    // todo kotlin 2.x explicit backing field
    private val _state = MutableStateFlow(ImageProcessingState())
    val state: StateFlow<ImageProcessingState> get() = _state.asStateFlow()

    // todo BaseMviViewmodel for state & effects & submit action
    private val _effects = MutableSharedFlow<ImageProcessingEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects = _effects.asSharedFlow()

    fun submitAction(action: ImageProcessingAction) = viewModelScope.launch {
        when (action) {
            is ImageProcessingAction.GalleryImagePicked -> processImage(
                imageUri = action.imageUri,
                inputImageResult = fileUtils.getInputImage(action.imageUri)
            )

            ImageProcessingAction.SelectImageFabClicked -> _state.update {
                it.copy(
                    sourcePickerOptions = listOf(FilePickerOption.Camera, FilePickerOption.Gallery)
                )
            }

            is ImageProcessingAction.PictureTaken -> handlePictureResult(action)

            ImageProcessingAction.HideImageSourcePicker -> hideImageSourcePicker()

            is ImageProcessingAction.FilePickerOptionSelected -> {
                hideImageSourcePicker()
                when (action.option) {
                    FilePickerOption.Camera -> openCamera()
                    FilePickerOption.Gallery -> openGallery()
                }
            }
        }
    }

    private fun hideImageSourcePicker() {
        _state.update {
            it.copy(sourcePickerOptions = emptyList())
        }
    }

    private fun openCamera() {
        viewModelScope.launch {
            val uri = fileUtils.getTmpCameraFileUri()
            _state.update { it.copy(cameraUri = uri.toString()) }
            setEffect(ImageProcessingEffect.TakePicture(uri.toString()))
        }
    }

    private suspend fun handlePictureResult(action: ImageProcessingAction.PictureTaken) {
        if (action.isImageSaved) {
            val uri = _state.value.cameraUri
            processImage(
                imageUri = uri,
                inputImageResult = fileUtils.getInputImage(uri)
            )
        } else {
            _state.update { it.copy(showError = true) }
        }
    }

    private suspend fun ImageProcessingViewModel.openGallery() {
        _state.update { it.copy(sourcePickerOptions = emptyList()) }
        setEffect(
            ImageProcessingEffect.OpenGallery(
                GalleryPickerOption.IMAGE_ONLY
            )
        )
    }

    private suspend fun setEffect(effect: ImageProcessingEffect) {
        _effects.emit(effect)
    }

    // todo some base progress watcher and withProgress extensions
    private fun processImage(
        imageUri: String,
        inputImageResult: EitherResult<InputImage>,
    ) = viewModelScope.launch {
        _state.update { it.copy(isProgressVisible = true) }
        inputImageResult
            .onSuccess { inputImage ->
                // todo lift logic to StateCase
                processInputImage(inputImage, imageUri)
            }
            .onError {
                _state.update {
                    it.copy(
                        showError = true,
                        isProgressVisible = false
                    )
                } // todo AppError class end different error types
            }
    }

    private suspend fun processInputImage(
        inputImage: InputImage,
        imageUri: String,
    ) = coroutineScope {
        either {
            val texts = async { textDetectionUseCase.call(inputImage).bind() }
            val objects = async { objectDetectionUseCase.call(inputImage).bind() }
            texts.await() to objects.await()
        }
            .onSuccess { (texts, objects) ->
                _state.update {
                    it.copy(
                        isProgressVisible = false,
                        showError = false,
                        detectedTextObjects = texts,
                        detectedObjects = objects,
                        imageState = ImageState(
                            height = inputImage.height,
                            width = inputImage.width,
                            uri = imageUri
                        )
                    )
                }
            }
            .onError {
                _state.update {
                    it.copy(
                        isProgressVisible = false,
                        imageState = ImageState(),
                        showError = true,
                        detectedTextObjects = emptyList(),
                        detectedObjects = emptyList()
                    )
                }
            }
    }
}
