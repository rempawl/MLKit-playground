package com.rempawl.image.processing

import android.graphics.RectF
import arrow.core.left
import arrow.core.right
import com.rempawl.core.ui.bottomsheet.GalleryPickerOption
import com.rempawl.core.ui.bottomsheet.ImageSourcePickerOption.CAMERA
import com.rempawl.core.ui.bottomsheet.ImageSourcePickerOption.GALLERY
import com.rempawl.core.kotlin.error.DURATION_ERROR_LONG
import com.rempawl.core.kotlin.error.ErrorManagerImpl
import com.rempawl.core.kotlin.progress.ProgressSemaphoreImpl
import com.rempawl.core.viewmodel.saveable.Saveable
import com.rempawl.image.processing.error.ImageNotSavedError
import com.rempawl.image.processing.error.ImageProcessingErrorMessageProvider
import com.rempawl.image.processing.model.DetectedObject
import com.rempawl.image.processing.model.DetectedTextObject
import com.rempawl.image.processing.model.ImageProcessingResult
import com.rempawl.image.processing.usecase.GetCameraPhotoUriUseCase
import com.rempawl.image.processing.usecase.ProcessImageUseCase
import com.rempawl.image.processing.viewmodel.ImageProcessingAction
import com.rempawl.image.processing.viewmodel.ImageProcessingEffect
import com.rempawl.image.processing.viewmodel.ImageProcessingState
import com.rempawl.image.processing.viewmodel.ImageProcessingViewModel
import com.rempawl.image.processing.viewmodel.ImageProcessingViewModel.Companion.KEY_SAVED_STATE_PROVIDER
import com.rempawl.image.processing.viewmodel.ImageProcessingViewModel.Companion.KEY_STATE
import com.rempawl.image.processing.viewmodel.ImageState
import com.rempawl.test.utils.BaseCoroutineTest
import com.rempawl.test.utils.coVerifyNever
import com.rempawl.test.utils.coVerifyOnce
import com.rempawl.test.utils.testEffects
import com.rempawl.test.utils.testState
import com.rempawl.test.utils.verifyNever
import com.rempawl.test.utils.verifyOnce
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/* ktlint-disable max-line-length */
@ExperimentalCoroutinesApi
class ImageProcessingViewModelTest : BaseCoroutineTest() {

    private val saveable = mockk<Saveable>(relaxUnitFun = true)
    private val getCameraUriUseCase = mockk<GetCameraPhotoUriUseCase>()
    private val imageProcessingUseCase = mockk<ProcessImageUseCase>()
    private val errorMessageProvider = mockk<ImageProcessingErrorMessageProvider> {
        every { getErrorMessageFor(any()) } returns "error"
    }
    private val errorManager = spyk(ErrorManagerImpl())

    private fun createSUT(
        textDetectionError: Throwable? = null,
        cameraUriError: Throwable? = null,
        savedState: ImageProcessingState? = null,
        processImageDelay: Long? = null,
    ): ImageProcessingViewModel {
        saveable.mock(savedState)
        imageProcessingUseCase.mock(textDetectionError, processImageDelay)
        getCameraUriUseCase.mock(cameraUriError)
        return ImageProcessingViewModel(
            processImageUseCase = imageProcessingUseCase,
            getCameraPhotoUriUseCase = getCameraUriUseCase,
            saveable = saveable,
            errorMessageProvider = errorMessageProvider,
            errorManager = errorManager,
            progressSemaphore = ProgressSemaphoreImpl()
        )
    }


    @Test
    fun `when initialized then empty state set`() = runTest {
        createSUT().testState {
            assertEquals(INITIAL_STATE, awaitItem())
        }
    }

    @Test
    fun `when select image fab clicked, then source picker options set`() = runTest {
        createSUT().testState {
            awaitItem().run {
                assertEquals(emptyList(), sourcePickerOptions)
                assertFalse(isSourcePickerVisible)
            }

            submitAction(ImageProcessingAction.SelectImageFabClicked)

            awaitItem().run {
                assertEquals(
                    listOf(CAMERA, GALLERY),
                    sourcePickerOptions
                )
                assertTrue(isSourcePickerVisible)
            }
            expectNoEvents()
        }
    }

    @Test
    fun `given source picker visible, when hide image source picker called, then source picker is hidden`() =
        runTest {
            createSUT().testState {
                submitAction(ImageProcessingAction.SelectImageFabClicked)
                expectMostRecentItem().run { assertTrue(isSourcePickerVisible) }

                submitAction(ImageProcessingAction.HideImageSourcePicker)

                awaitItem().run {
                    assertFalse(isSourcePickerVisible)
                    assertEquals(emptyList(), sourcePickerOptions)
                }
            }
        }

    @Test
    fun `given source picker visible, when gallery option selected, then source picker is hidden`() =
        runTest {
            createSUT().testState {
                submitAction(ImageProcessingAction.SelectImageFabClicked)
                expectMostRecentItem().run { assertTrue(isSourcePickerVisible) }

                submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(
                        GALLERY
                    )
                )

                awaitItem().run {
                    assertFalse(isSourcePickerVisible)
                    assertEquals(emptyList(), sourcePickerOptions)
                }
            }
        }

    @Test
    fun `given source picker visible, when camera option selected, then source picker is hidden`() =
        runTest {
            createSUT().testState {
                submitAction(ImageProcessingAction.SelectImageFabClicked)
                expectMostRecentItem().run { assertTrue(isSourcePickerVisible) }

                submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(
                        CAMERA
                    )
                )

                awaitItem().run {
                    assertFalse(isSourcePickerVisible)
                    assertEquals(emptyList(), sourcePickerOptions)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when camera option selected and photo uri retrieved,then camera uri is set `() =
        runTest {
            createSUT().testState {
                coVerifyNever { getCameraUriUseCase.call(Unit) }
                submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(
                        CAMERA
                    )
                )

                coVerifyOnce { getCameraUriUseCase.call(Unit) }
                expectMostRecentItem().run {
                    assertFalse(isSourcePickerVisible)
                    assertEquals(emptyList(), sourcePickerOptions)
                    assertEquals(CAMERA_URI_STRING, cameraUri)
                }
            }
        }

    @Test
    fun `when gallery option selected on image source picker,then OpenGallery effect is set with Image Only option `() =
        runTest {
            createSUT().testEffects {
                expectNoEvents()

                submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(
                        GALLERY
                    )
                )

                awaitItem().run {
                    assertEquals(
                        ImageProcessingEffect.OpenGallery(GalleryPickerOption.IMAGE_ONLY),
                        this
                    )
                }
            }
        }

    @Test
    fun `when camera option selected and photo uri retrieved,then TakePicture effect is set with correct uri `() =
        runTest {
            createSUT().testEffects {
                expectNoEvents()

                submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(
                        CAMERA
                    )
                )

                awaitItem().run {
                    assertIs<ImageProcessingEffect.TakePicture>(this)
                    assertEquals(ImageProcessingEffect.TakePicture(CAMERA_URI_STRING), this)
                }
            }
        }

    @Test
    fun `when camera option selected and photo uri retrieval fails, then error is shown and hidden after duration`() =
        runTest {
            createSUT(cameraUriError = FAKE_THROWABLE).testState {
                awaitItem().run { assertNull(error) }
                coVerifyNever { getCameraUriUseCase.call(Unit) }
                submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(
                        CAMERA
                    )
                )

                coVerifyOnce { getCameraUriUseCase.call(Unit) }
                awaitItem().run {
                    assertEquals("", cameraUri)
                    assertNotNull(error)
                }
                advanceTimeBy(DURATION_ERROR_LONG.inWholeMilliseconds + 1)
                assertNull(awaitItem().error)
            }
        }

    @Test
    fun `when camera picture taken but not saved, then ImageNotSaved error shown and hidden after duration`() =
        runTest {
            createSUT().testState {
                awaitItem().run { assertNull(error) }
                coVerifyNever { errorManager.addError(any()) }
                verifyNever { errorMessageProvider.getErrorMessageFor(ImageNotSavedError) }

                submitAction(ImageProcessingAction.PictureTaken(isImageSaved = false))

                verifyOnce { errorMessageProvider.getErrorMessageFor(ImageNotSavedError) }
                coVerifyOnce { errorManager.addError(any()) }
                awaitItem().run { assertNotNull(error) }
                advanceTimeBy(DURATION_ERROR_LONG.inWholeMilliseconds + 1)
                assertNull(awaitItem().error)
            }
        }

    @Test
    fun `given empty camera uri, when camera picture taken, then uri retrieved from savedState`() =
        runTest {
            createSUT().testState {
                saveable.mock(ImageProcessingState(cameraUri = SAVED_CAMERA_URI_STRING))
                coVerifyOnce {
                    saveable.getState<ImageProcessingState>(param = any())
                }
                awaitItem().run { assertEquals("", cameraUri) }

                submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))

                coVerify(exactly = 2) {
                    saveable.getState<ImageProcessingState>(param = any())
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given empty camera uri and no saved uri, when camera picture taken, then ImageNotSaved error is shown and hidden after duration`() =
        runTest {
            createSUT().testState {
                verifyNever { errorMessageProvider.getErrorMessageFor(any()) }
                awaitItem().run {
                    assertEquals("", cameraUri)
                    assertNull(error)
                }

                submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))

                verifyOnce { errorMessageProvider.getErrorMessageFor(ImageNotSavedError) }
                awaitItem().run { assertNotNull(error) }
                advanceTimeBy(DURATION_ERROR_LONG.inWholeMilliseconds + 1)
                assertNull(awaitItem().error)
            }
        }

    @Test
    fun `given empty camera uri, when camera picture taken, then uri retrieved from saved state is processed`() =
        runTest {
            createSUT().testState {
                saveable.mock(state = ImageProcessingState(cameraUri = SAVED_CAMERA_URI_STRING))
                awaitItem().run {
                    assertEquals("", cameraUri)
                    assertTrue(detectedObjects.isEmpty())
                    assertTrue(detectedTextObjects.isEmpty())
                    assertEquals(ImageState(), imageState)
                }

                submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))

                expectMostRecentItem().run {
                    assertTrue(detectedObjects.isNotEmpty())
                    assertTrue(detectedTextObjects.isNotEmpty())
                    assertEquals(
                        ImageState(
                            height = TEST_HEIGHT,
                            width = TEST_WIDTH,
                            uri = SAVED_CAMERA_URI_STRING
                        ),
                        imageState
                    )
                }
            }
        }

    @Test
    fun `when processing camera photo, then progress is shown and hidden`() =
        runTest {
            createSUT(processImageDelay = TEST_DELAY).testState {
                submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(CAMERA)
                )
                expectMostRecentItem().run { assertFalse(isProgressVisible) }

                submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))

                awaitItem().run { assertTrue(isProgressVisible) }
                advanceUntilIdle()
                expectMostRecentItem().run { assertFalse(isProgressVisible) }
            }
        }

    @Test
    fun `when camera photo processed, then imageState detected objects and texts are set `() =
        runTest {
            createSUT(processImageDelay = TEST_DELAY).testState {
                awaitItem().run {
                    assertEquals(emptyList(), detectedObjects)
                    assertEquals(emptyList(), detectedTextObjects)
                    assertEquals("", cameraUri)
                }

                submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(CAMERA)
                )
                expectMostRecentItem().run { assertEquals(CAMERA_URI_STRING, cameraUri) }
                submitAction(ImageProcessingAction.PictureTaken(true))

                awaitItem().run { assertTrue(isProgressVisible) }
                awaitItem().run {
                    assertEquals(
                        ImageState(
                            uri = CAMERA_URI_STRING,
                            height = TEST_HEIGHT,
                            width = TEST_WIDTH
                        ),
                        imageState
                    )
                    assertEquals(
                        listOf(
                            DetectedObject(
                                TEST_RECT, "label 0"
                            ),
                            DetectedObject(
                                TEST_RECT, "label 1"
                            ),
                            DetectedObject(
                                TEST_RECT, "label 2"
                            ),
                        ),
                        detectedObjects
                    )
                    assertEquals(
                        listOf(DetectedTextObject(TEST_RECT)), detectedTextObjects
                    )
                }
                awaitItem().run {
                    assertFalse(isProgressVisible)
                }
            }
        }

    @Test
    fun `when process image called, then text and object detection use cases called`() = runTest {
        val viewModel = createSUT()
        coVerifyNever {
            imageProcessingUseCase.call(any())
        }

        viewModel.submitAction(ImageProcessingAction.GalleryImagePicked("uri"))

        coVerifyOnce {
            imageProcessingUseCase.call(any())
        }
    }

    @Test
    fun `when image selected then progress is shown and uri is set`() = runTest {
        createSUT(processImageDelay = TEST_DELAY).testState {
            assertEquals(INITIAL_STATE, awaitItem())

            submitAction(
                ImageProcessingAction.GalleryImagePicked(
                    "uri",
                )
            )

            assertEquals(INITIAL_STATE.copy(isProgressVisible = true), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when image is processed, then detected objects and texts are set`() =
        runTest {
            createSUT(processImageDelay = TEST_DELAY).testState {
                assertEquals(INITIAL_STATE, awaitItem())

                submitAction(
                    ImageProcessingAction.GalleryImagePicked(
                        "uri",
                    )
                )

                assertEquals(INITIAL_STATE.copy(isProgressVisible = true), awaitItem())
                advanceUntilIdle()
                assertEquals(
                    ImageProcessingState(
                        isProgressVisible = false,
                        imageState = ImageState(
                            uri = "uri",
                            height = TEST_HEIGHT,
                            width = TEST_WIDTH
                        ),
                        detectedObjects = listOf(
                            DetectedObject(
                                TEST_RECT, "label 0"
                            ),
                            DetectedObject(
                                TEST_RECT, "label 1"
                            ),
                            DetectedObject(
                                TEST_RECT, "label 2"
                            ),
                        ),
                        detectedTextObjects = listOf(DetectedTextObject(TEST_RECT))
                    ),
                    expectMostRecentItem()
                )
            }
        }

    @Test
    fun `when error occurs then progress is hidden and error is shown and hidden after duration`() =
        runTest {
            createSUT(
                textDetectionError = FAKE_THROWABLE,
                processImageDelay = TEST_DELAY
            ).testState {
                assertEquals(INITIAL_STATE, awaitItem())

                submitAction(
                    ImageProcessingAction.GalleryImagePicked("uri")
                )
                assertTrue(awaitItem().isProgressVisible)
                assertNotNull(awaitItem().error)
                advanceTimeBy(DURATION_ERROR_LONG.inWholeMilliseconds + 1)
                awaitItem().run {
                    assertNull(error)
                    assertFalse(isProgressVisible)
                }
            }
        }

    @Test
    fun `when lifecycle stopped, then most recent state saved`() = runTest {
        createSUT().testState {
            coVerifyNever { saveable.saveState<ImageProcessingState>(any()) }

            submitAction(ImageProcessingAction.LifecycleStopped)

            coVerifyOnce {
                saveable.saveState(
                    Saveable.SaveStateParam(
                        state = expectMostRecentItem(),
                        keyProvider = KEY_SAVED_STATE_PROVIDER,
                        keyState = KEY_STATE
                    )
                )
            }
        }
    }

    @Test
    fun `when saved state retrieved, then it is set on init`() = runTest {
        val savedState = ImageProcessingState(
            cameraUri = CAMERA_URI_STRING,
            detectedObjects = listOf(DetectedObject(TEST_RECT, "label")),
            detectedTextObjects = listOf(DetectedTextObject(TEST_RECT)),
            imageState = ImageState(TEST_HEIGHT, TEST_WIDTH, CAMERA_URI_STRING),
        )
        createSUT(savedState = savedState).testState {
            assertEquals(savedState, awaitItem())

            expectNoEvents()
        }
    }

    private fun ProcessImageUseCase.mock(error: Throwable? = null, processImageDelay: Long?) {
        coEvery { call(any()) } coAnswers {
            processImageDelay?.let { delay(it) }
            error?.left() ?: ImageProcessingResult(
                detectedTextObjects = listOf(DetectedTextObject(TEST_RECT)),
                detectedObjects = FAKE_DETECTED_OBJECTS,
                imageWidth = TEST_WIDTH,
                imageHeight = TEST_HEIGHT
            ).right()
        }
    }

    private fun GetCameraPhotoUriUseCase.mock(
        cameraUriError: Throwable? = null,
    ) {
        coEvery { call(Unit) } returns (cameraUriError?.left() ?: CAMERA_URI_STRING.right())
    }

    private fun Saveable.mock(
        state: ImageProcessingState? = null,
    ) {
        coEvery { getState<ImageProcessingState>(any()) } returns state
    }

    /* ktlint-enable max-line-length */
    companion object {
        const val TEST_DELAY = 2L
        val FAKE_THROWABLE = Throwable("test")
        const val CAMERA_URI_STRING = "camera uri"
        const val SAVED_CAMERA_URI_STRING = "saved camera uri"
        val TEST_RECT = mockk<RectF>()
        val FAKE_DETECTED_OBJECTS = (0..2).map {
            DetectedObject(
                TEST_RECT, "label $it"
            )
        }

        val INITIAL_STATE = ImageProcessingState()
        const val TEST_HEIGHT = 3840
        const val TEST_WIDTH = 2160
    }
}