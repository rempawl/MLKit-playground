package com.rempawl.image.processing.viewmodel

import arrow.core.Either
import com.rempawl.bottomsheet.GalleryPickerOption
import com.rempawl.bottomsheet.ImageSourcePickerOption
import com.rempawl.core.kotlin.error.AppError
import com.rempawl.core.kotlin.error.ErrorManager
import com.rempawl.core.kotlin.error.UIError
import com.rempawl.core.kotlin.error.toUIError
import com.rempawl.core.kotlin.extensions.onError
import com.rempawl.core.kotlin.extensions.onSuccess
import com.rempawl.core.viewmodel.mvi.BaseMVIViewModel
import com.rempawl.core.viewmodel.saveable.Saveable
import com.rempawl.image.processing.usecase.GetCameraPhotoUriUseCase
import com.rempawl.image.processing.usecase.ProcessImageUseCase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel for image processing.
 *
 * This ViewModel manages the state and logic for image processing operations,
 * including selecting an image source, taking pictures, processing images,
 * and handling errors. It follows the MVI architecture pattern.
 */
class ImageProcessingViewModel(
    private val processImageUseCase: ProcessImageUseCase,
    private val getCameraPhotoUriUseCase: GetCameraPhotoUriUseCase,
    private val saveable: Saveable,
    errorManager: ErrorManager
) : BaseMVIViewModel<ImageProcessingState, ImageProcessingAction, ImageProcessingEffect>(
    errorManager,
    ImageProcessingState()
) {

    override fun doOnStateSubscription(): suspend () -> Unit = {
        retrieveSavedState()?.let { savedState -> setState { savedState } }
    }

    override fun handleError(
        appError: Either<Unit, AppError>,
        state: ImageProcessingState
    ): ImageProcessingState = state.copy(showError = appError.isRight())

    override suspend fun handleActions(action: ImageProcessingAction) {
        when (action) {
            is ImageProcessingAction.GalleryImagePicked -> processImage(imageUri = action.imageUri)

            ImageProcessingAction.SelectImageFabClicked -> setState {
                copy(
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
                saveable.saveState(
                    Saveable.SaveStateParam(
                        state = state.value,
                        keyProvider = KEY_SAVED_STATE_PROVIDER,
                        keyState = KEY_STATE
                    )
                )
            }
        }
    }

    private suspend fun retrieveSavedState(): ImageProcessingState? = saveable.getState(
        Saveable.GetSavedStateParam(
            keyProvider = KEY_SAVED_STATE_PROVIDER,
            keyState = KEY_STATE
        )
    )

    private fun hideImageSourcePicker() {
        setState {
            copy(sourcePickerOptions = emptyList())
        }
    }

    private suspend fun tryOpenCamera() {
        getCameraPhotoUriUseCase.call(Unit)
            .onSuccess { uri ->
                setState { copy(cameraUri = uri) }
                setEffect { ImageProcessingEffect.TakePicture(uri) }
            }.onError {
                addError(it.toUIError(DURATION_ERROR))
            }
    }

    private suspend fun handlePictureTakenResult(action: ImageProcessingAction.PictureTaken) {
        val cameraUri = state.value.cameraUri.ifEmpty {
            retrieveSavedState()?.cameraUri.orEmpty()
        }
        if (action.isImageSaved && cameraUri.isNotEmpty()) {
            processImage(imageUri = cameraUri)
        } else {
            addError(UIError(null, DURATION_ERROR)) // todo DefaultError interface
        }
    }

    private suspend fun openGallery() {
        setEffect {
            ImageProcessingEffect.OpenGallery(GalleryPickerOption.IMAGE_ONLY)
        }
    }


    // todo some base progress watcher and withProgress extensions
    private suspend fun processImage(imageUri: String) {
        setState { copy(isProgressVisible = true) }
        processInputImage(imageUri)
    }

    private suspend fun processInputImage(imageUri: String) = coroutineScope {
        processImageUseCase.call(imageUri)
            .onSuccess { (texts, objects, imgWidth, imgHeight) ->
                setState {
                    copy(
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
                addError(it.toUIError(DURATION_ERROR))
                setState {
                    copy(isProgressVisible = false)
                }
            }
    }

    companion object {
        private val className by lazy { this::class.java.name }
        internal val KEY_STATE = "${className}_state"
        internal val KEY_SAVED_STATE_PROVIDER = "${className}_state_provider"
        val DURATION_ERROR = 5.seconds
    }
}
