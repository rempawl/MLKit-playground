package com.rempawl.image.processing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rempawl.core.kotlin.onError
import com.rempawl.core.kotlin.onSuccess
import com.rempawl.core.viewmodel.usecase.GetSavedStateUseCase.SavedStateParam
import com.rempawl.core.viewmodel.usecase.SaveStateUseCase
import com.rempawl.image.processing.core.GalleryPickerOption
import com.rempawl.image.processing.core.ImageSourcePickerOption
import com.rempawl.image.processing.usecase.GetCameraPhotoUriUseCase
import com.rempawl.image.processing.usecase.GetImageProcessingSavedStateUseCase
import com.rempawl.image.processing.usecase.ProcessImageUseCase
import com.rempawl.image.processing.usecase.SaveImageProcessingStateUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// todo add saveable interface and use delegate on BaseViewModel
class ImageProcessingViewModel(
    private val processImageUseCase: ProcessImageUseCase,
    private val getCameraPhotoUriUseCase: GetCameraPhotoUriUseCase,
    private val saveStateUseCase: SaveImageProcessingStateUseCase,
    private val getSavedStateUseCase: GetImageProcessingSavedStateUseCase,
) : ViewModel() {

    // todo kotlin 2.x explicit backing field
    private val _state = MutableStateFlow(ImageProcessingState())
    val state: StateFlow<ImageProcessingState> get() = _state.asStateFlow()

    // todo BaseMviViewmodel for state & effects & submit action
    private val _effects = MutableSharedFlow<ImageProcessingEffect>(
        extraBufferCapacity = 5, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            retrieveSavedState()?.let { savedState -> _state.update { savedState } }
        }
    }

    fun submitAction(action: ImageProcessingAction) = viewModelScope.launch {
        when (action) {
            is ImageProcessingAction.GalleryImagePicked -> processImage(imageUri = action.imageUri)

            ImageProcessingAction.SelectImageFabClicked -> _state.update {
                it.copy(
                    sourcePickerOptions = listOf(
                        ImageSourcePickerOption.CAMERA, ImageSourcePickerOption.GALLERY
                    )
                )
            }

            is ImageProcessingAction.PictureTaken -> handlePictureTakenResult(action)

            ImageProcessingAction.HideImageSourcePicker -> hideImageSourcePicker()

            is ImageProcessingAction.ImageSourcePickerOptionSelected -> {
                hideImageSourcePicker()
                when (action.option) {
                    ImageSourcePickerOption.CAMERA -> tryOpenCamera()
                    ImageSourcePickerOption.GALLERY -> openGallery()
                }
            }

            ImageProcessingAction.LifecycleStopped -> {
                saveStateUseCase.call(
                    SaveStateUseCase.Param(
                        state = state.value,
                        keyProvider = KEY_SAVED_STATE_PROVIDER,
                        keyState = KEY_STATE
                    )
                )
            }
        }
    }

    private suspend fun retrieveSavedState(): ImageProcessingState? = getSavedStateUseCase
        .call(
            SavedStateParam(
                keyProvider = KEY_SAVED_STATE_PROVIDER,
                keyState = KEY_STATE
            )
        )

    private fun hideImageSourcePicker() {
        _state.update {
            it.copy(sourcePickerOptions = emptyList())
        }
    }

    private suspend fun tryOpenCamera() {
        getCameraPhotoUriUseCase.call(Unit).onSuccess { uri ->
            _state.update { it.copy(cameraUri = uri) }
            setEffect(ImageProcessingEffect.TakePicture(uri))
        }.onError {
            _state.update { it.copy(showError = true) } // todo UriError
        }
    }

    private suspend fun handlePictureTakenResult(action: ImageProcessingAction.PictureTaken) {
        val cameraUri = state.value.cameraUri.ifEmpty {
            retrieveSavedState()?.cameraUri.orEmpty()
        }

        if (action.isImageSaved && cameraUri.isNotEmpty()) {
            processImage(imageUri = cameraUri)
        } else {
            _state.update { it.copy(showError = true) }
        }
    }

    private suspend fun openGallery() {
        setEffect(
            ImageProcessingEffect.OpenGallery(GalleryPickerOption.IMAGE_ONLY)
        )
    }

    private suspend fun setEffect(effect: ImageProcessingEffect) {
        _effects.emit(effect) // todo base viewmodel
    }

    // todo some base progress watcher and withProgress extensions
    private suspend fun processImage(imageUri: String) {
        _state.update { it.copy(isProgressVisible = true) }
        processInputImage(imageUri)
    }

    private suspend fun processInputImage(imageUri: String) = coroutineScope {
        processImageUseCase.call(imageUri)
            .onSuccess { (texts, objects, imgWidth, imgHeight) ->
                _state.update {
                    it.copy(
                        isProgressVisible = false,
                        showError = false,
                        detectedTextObjects = texts,
                        detectedObjects = objects,
                        imageState = ImageState(
                            height = imgHeight,
                            width = imgWidth,
                            uri = imageUri
                        )
                    )
                }
            }.onError {
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

    companion object {
        private val className by lazy { this::class.java.name }
        internal val KEY_STATE = "${className}_state"
        internal val KEY_SAVED_STATE_PROVIDER = "${className}_state_provider"
    }
}
