package com.rempawl.image.processing

import android.graphics.RectF
import app.cash.turbine.test
import arrow.core.right
import com.rempawl.image.processing.core.DispatchersProvider
import com.rempawl.image.processing.usecase.ObjectDetectionUseCase
import com.rempawl.image.processing.usecase.TextDetectionUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ImageProcessingViewModelTest : BaseCoroutineTest() {

    private val objectDetectionUseCase = mockk<ObjectDetectionUseCase>() {
        coEvery { call(any()) } coAnswers {
            (0..2).map {
                DetectedObject(
                    TEST_RECT,
                    "label $it"
                )
            }
                .right()
        }
    }

    private val textDetectionUseCase = mockk<TextDetectionUseCase>() {
        coEvery { call(any()) } coAnswers {
            delay(DELAY)
            listOf(DetectedTextObject(TEST_RECT)).right()
        }
    }

    private fun createSUT(delay: Long? = null, error: Throwable? = null): ImageProcessingViewModel {
        // todo error test and delay
        val viewModel = ImageProcessingViewModel(
            objectDetectionUseCase = objectDetectionUseCase,
            textDetectionUseCase = textDetectionUseCase,
            dispatchersProvider = object : DispatchersProvider {
                override val io: CoroutineDispatcher
                    get() = testDispatcher
                override val main: CoroutineDispatcher
                    get() = testDispatcher
                override val default: CoroutineDispatcher
                    get() = testDispatcher
            }
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
    fun `when image selected then progress is shown and uri is set`() = runTest {
        val viewModel = createSUT()
        viewModel.state.test {
            assertEquals(INITIAL_STATE, awaitItem())
            viewModel.processImage(mockk(), "uri")

            assertEquals(
                INITIAL_STATE.copy(isProgressVisible = true, imageUri = "uri"),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when object detection is finished but text detection still runs, then progress is visible and detected objects are set`() =
        runTest {
            val viewModel = createSUT()
            viewModel.state.test {
                assertEquals(INITIAL_STATE, awaitItem())

                viewModel.processImage(mockk(), "uri")

                assertEquals(
                    INITIAL_STATE.copy(isProgressVisible = true, imageUri = "uri"),
                    awaitItem()
                )

                assertEquals(
                    INITIAL_STATE.copy(
                        isProgressVisible = true,
                        imageUri = "uri",
                        detectedObjects = listOf(
                            DetectedObject(
                                TEST_RECT,
                                "label 0"
                            ),
                            DetectedObject(
                                TEST_RECT,
                                "label 1"
                            ),
                            DetectedObject(
                                TEST_RECT,
                                "label 2"
                            ),
                        ),
                        detectedTextObjects = emptyList()
                    ),
                    awaitItem()
                )
            }
        }

    @Test
    fun `when image is processed then progress is hidden and detected objects and texts are set`() =
        runTest {
            val viewModel = createSUT()
            viewModel.state.test {
                assertEquals(INITIAL_STATE, awaitItem())

                viewModel.processImage(mockk(), "uri")

                assertEquals(
                    INITIAL_STATE.copy(isProgressVisible = true, imageUri = "uri"),
                    awaitItem()
                )

                advanceTimeBy(DELAY + 1)
                assertEquals(
                    INITIAL_STATE.copy(
                        isProgressVisible = false,
                        imageUri = "uri",
                        detectedObjects = listOf(
                            DetectedObject(
                                TEST_RECT,
                                "label 0"
                            ),
                            DetectedObject(
                                TEST_RECT,
                                "label 1"
                            ),
                            DetectedObject(
                                TEST_RECT,
                                "label 2"
                            ),
                        ),
                        detectedTextObjects = listOf(DetectedTextObject(TEST_RECT))
                    ),
                    expectMostRecentItem()
                )
            }
        }

    companion object {
        val TEST_RECT = mockk<RectF>()
        val INITIAL_STATE = ImageProcessingState()
        const val DELAY = 10L
    }
}