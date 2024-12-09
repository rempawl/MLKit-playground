package com.rempawl.image.processing

import android.graphics.RectF
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.core.FileUtils
import com.rempawl.image.processing.core.GalleryPickerOption
import com.rempawl.image.processing.core.ImageSourcePickerOption.CAMERA
import com.rempawl.image.processing.core.ImageSourcePickerOption.GALLERY
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ImageProcessingViewModelTest : BaseCoroutineTest() {

    private val objectDetectionUseCase = mockk<ObjectDetectionUseCase> {
        coEvery { call(any()) } returns (0..2).map {
            DetectedObject(
                TEST_RECT, "label $it"
            )
        }.right()
    }

    private val inputImage = mockk<InputImage> {
        every { height } returns TEST_HEIGHT
        every { width } returns TEST_WIDTH
    }

    private val fileUtils = mockk<FileUtils>()

    private val textDetectionUseCase = mockk<TextDetectionUseCase>()

    private fun createSUT(
        textDetectionError: Throwable? = null,
        inputImageError: Throwable? = null,
        cameraUriError: Throwable? = null,
        inputImageDelay: Long? = null,
    ): ImageProcessingViewModel {
        textDetectionUseCase.mock(textDetectionError)
        fileUtils.mock(inputImageError, cameraUriError, inputImageDelay)
        val viewModel = ImageProcessingViewModel(
            objectDetectionUseCase = objectDetectionUseCase,
            textDetectionUseCase = textDetectionUseCase,
            fileUtils = fileUtils
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
                coVerifyNever { fileUtils.getTmpCameraFileUriString() }
                viewModel.submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(
                        CAMERA
                    )
                )

                coVerifyOnce { fileUtils.getTmpCameraFileUriString() }
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
                coVerifyNever { fileUtils.getTmpCameraFileUriString() }
                viewModel.submitAction(
                    ImageProcessingAction.ImageSourcePickerOptionSelected(
                        CAMERA
                    )
                )

                coVerifyOnce { fileUtils.getTmpCameraFileUriString() }
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
    fun `given camera picture taken ,when input image retrieval fails, then hide progress and show error`() =
        runTest {
            val viewModel =
                createSUT(inputImageError = FAKE_THROWABLE, inputImageDelay = TEST_DELAY)
            viewModel.state.test {
                awaitItem().run { assertFalse(showError) }

                viewModel.submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))
                awaitItem().run { assertTrue(isProgressVisible) }

                advanceTimeBy(TEST_DELAY)
                awaitItem().run {
                    assertTrue(showError)
                    assertFalse(isProgressVisible)
                }
            }
        }

    @Test
    fun `when processing camera photo, then progress is shown and hidden`() =
        runTest {
            val viewModel = createSUT()
            viewModel.state.test {
                awaitItem().run { assertFalse(isProgressVisible) }

                viewModel.submitAction(ImageProcessingAction.PictureTaken(isImageSaved = true))

                awaitItem().run { assertTrue(isProgressVisible) }
                awaitItem().run { assertFalse(isProgressVisible) }
            }
        }

    @Test
    fun `when camera photo processed, then imageState detected objects and texts are set `() =
        runTest {
            val viewModel = createSUT()
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
            objectDetectionUseCase.call(any())
            textDetectionUseCase.call(any())
        }

        viewModel.submitAction(ImageProcessingAction.GalleryImagePicked("uri"))

        coVerifyOnce {
            objectDetectionUseCase.call(any())
            textDetectionUseCase.call(any())
        }
    }

    @Test
    fun `when retrieving input image fails, then no use cases called`() = runTest {
        val viewModel = createSUT(inputImageError = FAKE_THROWABLE)

        viewModel.submitAction(ImageProcessingAction.GalleryImagePicked("uri"))

        coVerifyNever {
            objectDetectionUseCase.call(any())
            textDetectionUseCase.call(any())
        }
    }

    @Test
    fun `when image selected then progress is shown and uri is set`() = runTest {
        val viewModel = createSUT()
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
            val viewModel = createSUT()
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
        val viewModel = createSUT(textDetectionError = FAKE_THROWABLE)
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
    fun `when retrieving input image fails, then error is shown`() = runTest {
        val viewModel = createSUT(inputImageError = FAKE_THROWABLE)
        viewModel.state.test {
            assertEquals(INITIAL_STATE, awaitItem())

            viewModel.submitAction(
                ImageProcessingAction.GalleryImagePicked(
                    "uri",
                )
            )

            assertEquals(
                INITIAL_STATE.copy(isProgressVisible = false, showError = true),
                awaitItem()
            )
        }
    }

    private fun TextDetectionUseCase.mock(error: Throwable? = null) {
        coEvery { call(any()) } answers {
            error?.left() ?: listOf(DetectedTextObject(TEST_RECT)).right()
        }
    }

    private fun FileUtils.mock(
        inputImageError: Throwable? = null,
        cameraUriError: Throwable? = null,
        inputImageDelay: Long? = null,
    ) {
        coEvery { getInputImage(any<String>()) } coAnswers {
            if (inputImageDelay != null) delay(inputImageDelay)
            (inputImageError?.left() ?: inputImage.right())
        }
        coEvery { getTmpCameraFileUriString() } returns (cameraUriError?.left()
            ?: CAMERA_URI_STRING.right())
    }

    companion object {
        const val TEST_DELAY = 2L
        val FAKE_THROWABLE = Throwable("test")
        const val CAMERA_URI_STRING = "camera uri"
        val TEST_RECT = mockk<RectF>()
        val INITIAL_STATE = ImageProcessingState()
        const val TEST_HEIGHT = 3840
        const val TEST_WIDTH = 2160
    }
}