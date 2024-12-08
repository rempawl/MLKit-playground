package com.rempawl.image.processing

import android.graphics.RectF
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


// todo add tests
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

    private val inputImageProvider = mockk<() -> InputImage> {
        every { this@mockk.invoke() } answers { inputImage }
    }

    private val textDetectionUseCase = mockk<TextDetectionUseCase>()

    private fun createSUT(error: Throwable? = null): ImageProcessingViewModel {
        textDetectionUseCase.mock(error)
        val viewModel = ImageProcessingViewModel(
            objectDetectionUseCase = objectDetectionUseCase,
            textDetectionUseCase = textDetectionUseCase,
            fileUtils = mockk() {
                coEvery { getInputImage(any<String>()) } returns inputImage.right()
            } // todo left
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
    fun `when empty uri passed to process image, then no use cases called`() = runTest {
        val viewModel = createSUT()

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
        val viewModel = createSUT(Throwable("test"))
        viewModel.state.test {
            assertEquals(INITIAL_STATE, awaitItem())

            viewModel.submitAction(
                ImageProcessingAction.GalleryImagePicked(
                    "uri",
                )
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
        val viewModel = createSUT(Throwable("test"))
        // todo mock fileUtils.getInputImage error
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

    companion object {
        val TEST_RECT = mockk<RectF>()
        val INITIAL_STATE = ImageProcessingState()
        const val TEST_HEIGHT = 3840
        const val TEST_WIDTH = 2160
    }
}