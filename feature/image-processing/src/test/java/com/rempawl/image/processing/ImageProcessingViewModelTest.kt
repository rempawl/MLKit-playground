package com.rempawl.image.processing

import android.graphics.RectF
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import com.rempawl.core.viewmodel.saveable.Saveable
import com.rempawl.bottomsheet.GalleryPickerOption
import com.rempawl.bottomsheet.ImageSourcePickerOption.CAMERA
import com.rempawl.bottomsheet.ImageSourcePickerOption.GALLERY
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
import com.rempawl.test.utils.coVerifyNever
import com.rempawl.test.utils.coVerifyOnce
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/* ktlint-disable max-line-length */
class ImageProcessingViewModelTest : com.rempawl.test.utils.BaseCoroutineTest() {

    private val saveable = mockk<Saveable>(relaxUnitFun = true)
    private val getCameraUriUseCase = mockk<GetCameraPhotoUriUseCase>()
    private val imageProcessingUseCase = mockk<ProcessImageUseCase>()

    private fun createSUT(
        textDetectionError: Throwable? = null,
        cameraUriError: Throwable? = null,
        savedState: ImageProcessingState? = null,
        processImageDelay: Long? = null,
    ): ImageProcessingViewModel {
        saveable.mock(savedState)
        imageProcessingUseCase.mock(textDetectionError, processImageDelay)
        getCameraUriUseCase.mock(cameraUriError)
        val viewModel = ImageProcessingViewModel(
            processImageUseCase = imageProcessingUseCase,
            getCameraPhotoUriUseCase = getCameraUriUseCase,
            saveable = saveable
        )
        return viewModel
    }

    @Test
    fun `when initialized then empty state set`() = runTest {
        val viewModel = createSUT()
        viewModel.state.test {
            assertEquals(INITIAL_STATE, awaitItem())
        }
    }

    @Test
    fun `when select image fab clicked, then source picker options set`() = runTest {
        val viewmodel = createSUT() // todo extensions for state & effects testing
        viewmodel.state.test {
            awaitItem().run {
                assertEquals(emptyList(), sourcePickerOptions)
                assertFalse(isSourcePickerVisible)
            }

            viewmodel.submitAction(ImageProcessingAction.SelectImageFabClicked)

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
            val viewModel = createSUT()
            viewModel.state.test {
                viewModel.submitAction(ImageProcessingAction.SelectImageFabClicked)
                expectMostRecentItem().run { assertTrue(isSourcePickerVisible) }

                viewModel.submitAction(ImageProcessingAction.HideImageSourcePicker)

                awaitItem().run {
                    assertFalse(isSourcePickerVisible)
                    assertEquals(emptyList(), sourcePickerOptions)
                }
            }
        }

    @Test
    fun `given source picker visible, when gallery option selected, then source picker is hidden`() =
        runTest {
            val viewModel = createSUT()
            viewModel.state.test {
                viewModel.submitAction(ImageProcessingAction.SelectImageFabClicked)
                expectMostRecentItem().run { assertTrue(isSourcePickerVisible) }

                viewModel.submitAction(
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
            val viewModel = createSUT()
            viewModel.state.test {
                viewModel.submitAction(ImageProcessingAction.SelectImageFabClicked)
                expectMostRecentItem().run { assertTrue(isSourcePickerVisible) }

                viewModel.submitAction(
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
            val viewModel = createSUT()
            viewModel.state.test {
                coVerifyNever { getCameraUriUseCase.call(Unit) }
                viewModel.submitAction(
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
            val viewModel = createSUT()
            viewModel.effects.test {
                expectNoEvents()

                viewModel.submitAction(
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
            val viewModel = createSUT()
            viewModel.effects.test {
                expectNoEvents()

                viewModel.submitAction(
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
    fun `when camera option selected and photo uri retrieval fails, then error is shown`() =
        runTest {
            val viewModel = createSUT(cameraUriError = FAKE_THROWABLE)
            viewModel.state.test {
                awaitItem().run { assertFalse(showError) }
                coVerifyNever { getCameraUriUseCase.call(Unit) }
                viewModel.submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(
                        CAMERA
                    )
                )

                coVerifyOnce { getCameraUriUseCase.call(Unit) }
                awaitItem().run {
                    assertEquals("", cameraUri)
                    assertTrue(showError)
                }
            }
        }

    @Test
    fun `when camera picture taken but not saved, then show error`() = runTest {
        val viewModel = createSUT()
        viewModel.state.test {
            awaitItem().run { assertFalse(showError) }

            viewModel.submitAction(ImageProcessingAction.PictureTaken(isImageSaved = false))

            awaitItem().run { assertTrue(showError) }
        }
    }

    @Test
    fun `given empty camera uri, when camera picture taken, then uri retrieved from savedState `() =
        runTest {
            val viewModel = createSUT()
            viewModel.state.test {
                saveable.mock(ImageProcessingState(cameraUri = SAVED_CAMERA_URI_STRING))
                coVerifyOnce {
                    saveable.getState<ImageProcessingState>(param = any())
                }
                awaitItem().run { assertEquals("", cameraUri) }

                viewModel.submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))

                coVerify(exactly = 2) {
                    saveable.getState<ImageProcessingState>(param = any())
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given empty camera uri and no saved uri, when camera picture taken, then error is shown`() =
        runTest {
            val viewModel = createSUT()
            viewModel.state.test {
                awaitItem().run {
                    assertEquals("", cameraUri)
                    assertFalse(showError)
                }

                viewModel.submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))

                awaitItem().run { assertTrue(showError) }
            }
        }

    @Test
    fun `given empty camera uri, when camera picture taken, then uri retrieved from saved state is processed`() =
        runTest {
            val viewModel = createSUT()
            viewModel.state.test {
                saveable.mock(state = ImageProcessingState(cameraUri = SAVED_CAMERA_URI_STRING))
                awaitItem().run {
                    assertEquals("", cameraUri)
                    assertTrue(detectedObjects.isEmpty())
                    assertTrue(detectedTextObjects.isEmpty())
                    assertEquals(ImageState(), imageState)
                }

                viewModel.submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))

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
            val viewModel = createSUT(processImageDelay = TEST_DELAY)
            viewModel.state.test {
                viewModel.submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(CAMERA)
                )
                expectMostRecentItem().run { assertFalse(isProgressVisible) }

                viewModel.submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))

                awaitItem().run { assertTrue(isProgressVisible) }
                awaitItem().run { assertFalse(isProgressVisible) }
            }
        }

    @Test
    fun `when camera photo processed, then imageState detected objects and texts are set `() =
        runTest {
            val viewModel = createSUT(processImageDelay = TEST_DELAY)
            viewModel.state.test {
                awaitItem().run {
                    assertEquals(emptyList(), detectedObjects)
                    assertEquals(emptyList(), detectedTextObjects)
                    assertEquals("", cameraUri)
                }

                viewModel.submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(CAMERA)
                )
                expectMostRecentItem().run { assertEquals(CAMERA_URI_STRING, cameraUri) }
                viewModel.submitAction(ImageProcessingAction.PictureTaken(true))

                awaitItem().run { assertTrue(isProgressVisible) }
                awaitItem().run {
                    assertFalse(isProgressVisible)
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
        val viewModel = createSUT(processImageDelay = TEST_DELAY)
        viewModel.state.test {
            assertEquals(INITIAL_STATE, awaitItem())

            viewModel.submitAction(
                ImageProcessingAction.GalleryImagePicked(
                    "uri",
                )
            )

            assertEquals(INITIAL_STATE.copy(isProgressVisible = true), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when image is processed, then progress is hidden and detected objects and texts are set`() =
        runTest {
            val viewModel = createSUT(processImageDelay = TEST_DELAY)
            viewModel.state.test {
                assertEquals(INITIAL_STATE, awaitItem())

                viewModel.submitAction(
                    ImageProcessingAction.GalleryImagePicked(
                        "uri",
                    )
                )

                assertEquals(INITIAL_STATE.copy(isProgressVisible = true), awaitItem())
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
                    awaitItem()
                )
            }
        }

    @Test
    fun `when error occurs then progress is hidden and error is shown`() = runTest {
        val viewModel = createSUT(
            textDetectionError = FAKE_THROWABLE,
            processImageDelay = TEST_DELAY
        )
        viewModel.state.test {
            assertEquals(INITIAL_STATE, awaitItem())

            viewModel.submitAction(
                ImageProcessingAction.GalleryImagePicked("uri")
            )

            assertEquals(INITIAL_STATE.copy(isProgressVisible = true), awaitItem())
            assertEquals(
                INITIAL_STATE.copy(isProgressVisible = false, showError = true),
                awaitItem()
            )
        }
    }

    @Test
    fun `when lifecycle stopped, then most recent state saved`() = runTest {
        val viewModel = createSUT()
        viewModel.state.test {
            coVerifyNever { saveable.saveState<ImageProcessingState>(any()) }

            viewModel.submitAction(ImageProcessingAction.LifecycleStopped)

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
        val viewModel = createSUT(savedState = savedState)
        viewModel.state.test {
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