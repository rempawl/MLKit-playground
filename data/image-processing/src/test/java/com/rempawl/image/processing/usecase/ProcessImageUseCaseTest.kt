package com.rempawl.image.processing.usecase

import android.graphics.RectF
import arrow.core.left
import arrow.core.right
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.model.DetectedObject
import com.rempawl.image.processing.model.DetectedTextObject
import com.rempawl.test.utils.BaseCoroutineTest
import com.rempawl.test.utils.coVerifyNever
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessImageUseCaseTest : BaseCoroutineTest() {

    private val inputImage = mockk<InputImage> {
        every { height } returns TEST_HEIGHT
        every { width } returns TEST_WIDTH
    }

    private val objectDetectionUseCase = mockk<ObjectDetectionUseCase>()
    private val textDetectionUseCase = mockk<TextDetectionUseCase>()
    private val getInputImageUseCase = mockk<GetInputImageUseCase>()

    private fun createSUT(
        inputImageError: Throwable? = null,
        objectDetectionError: Throwable? = null,
        textDetectionError: Throwable? = null,
    ): ProcessImageUseCase {
        getInputImageUseCase.mock(inputImageError)
        textDetectionUseCase.mock(textDetectionError)
        objectDetectionUseCase.mock(objectDetectionError)
        return ProcessImageUseCase(
            textDetectionUseCase = textDetectionUseCase,
            objectDetectionUseCase = objectDetectionUseCase,
            getInputImageUseCase = getInputImageUseCase
        )
    }

    @Test
    fun `when input image error, then error is returned`() = runTest {
        createSUT(inputImageError = FAKE_THROWABLE).run {
            val result = call("uri")

            result.run {
                assertTrue(isLeft())
                this.onLeft {
                    assertEquals(FAKE_THROWABLE, it)
                }
            }
        }
    }

    @Test
    fun `when object detection error, then error is returned`() = runTest {
        createSUT(objectDetectionError = FAKE_THROWABLE).run {
            val result = call("uri")

            result.run {
                assertTrue(isLeft())
                this.onLeft {
                    assertEquals(FAKE_THROWABLE, it)
                }
            }
        }
    }

    @Test
    fun `when text detection error, then error is returned`() = runTest {
        createSUT(textDetectionError = FAKE_THROWABLE).run {
            val result = call("uri")

            result.run {
                assertTrue(isLeft())
                this.onLeft {
                    assertEquals(FAKE_THROWABLE, it)
                }
            }
        }
    }

    @Test
    fun `when image processed, then correct result is returned`() = runTest {
        createSUT().run {
            val result = call("uri")

            result.run {
                assertTrue(isRight())
                this.onRight {
                    assertEquals(FAKE_DETECTED_OBJECTS, it.detectedObjects)
                    assertEquals(listOf(DetectedTextObject(TEST_RECT)), it.detectedTextObjects)
                    assertEquals(TEST_WIDTH, it.imageWidth)
                    assertEquals(TEST_HEIGHT, it.imageHeight)
                }
            }
        }
    }

    @Test
    fun `when use case is called, then correct input image, text and object detection use cases are called`() =
        runTest {
            val useCase = createSUT()
            coVerifyNever {
                getInputImageUseCase.call(any())
                objectDetectionUseCase.call(any())
                textDetectionUseCase.call(any())
            }

            useCase.call("uri")

            coVerifySequence {
                getInputImageUseCase.call(any())
                textDetectionUseCase.call(any())
                objectDetectionUseCase.call(any())
            }
        }

    private fun ObjectDetectionUseCase.mock(error: Throwable? = null) {
        coEvery { call(any()) } answers { error?.left() ?: FAKE_DETECTED_OBJECTS.right() }

    }

    private fun TextDetectionUseCase.mock(error: Throwable?) {
        coEvery { call(any()) } answers {
            error?.left() ?: listOf(DetectedTextObject(TEST_RECT)).right()
        }
    }

    private fun GetInputImageUseCase.mock(throwable: Throwable? = null) {
        coEvery { call(any()) } answers { throwable?.left() ?: inputImage.right() }


    }

    companion object {
        val FAKE_THROWABLE = Throwable("test")
        val TEST_RECT = mockk<RectF>()
        val FAKE_DETECTED_OBJECTS = (0..2).map {
            DetectedObject(
                TEST_RECT, "label $it"
            )
        }
        const val TEST_HEIGHT = 3840
        const val TEST_WIDTH = 2160
    }

}