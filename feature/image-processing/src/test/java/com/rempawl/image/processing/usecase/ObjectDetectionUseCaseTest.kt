package com.rempawl.image.processing.usecase

import android.graphics.Rect
import arrow.core.left
import arrow.core.right
import com.google.mlkit.vision.objects.DetectedObject
import com.rempawl.image.processing.BaseCoroutineTest
import com.rempawl.image.processing.coVerifyNever
import com.rempawl.image.processing.coVerifyOnce
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ObjectDetectionUseCaseTest : BaseCoroutineTest() {

    private val repository = mockk<MLKitDetectionRepository>()

    private fun createSUT(
        result: List<DetectedObject>,
        error: Throwable? = null,
    ): ObjectDetectionUseCase {
        repository.mock(result, error)
        return ObjectDetectionUseCase(repository)
    }

    @Test
    fun `when use case called, then repository method called once`() = runTest {
        createSUT(emptyList()).run {
            coVerifyNever { repository.detectObjects(any()) }

            call(mockk())

            coVerifyOnce { repository.detectObjects(any()) }
        }
    }

    @Test
    fun `when detected object has many labels, then correct labels text`() = runTest {
        val sut = createSUT(listOf(TEST_OBJECT_MANY_LABELS))

        val res = sut.call(mockk())

        assertTrue(res.isRight())
        res.onRight {
            assertEquals(1, it.size)
            assertEquals("book - 60%, pen - 80%", it.first().labels)
        }
    }

    @Test
    fun `when detected object has no labels, then correct labels text`() = runTest {
        val sut = createSUT(listOf(TEST_OBJECT_NO_LABELS))

        val res = sut.call(mockk())

        assertTrue(res.isRight())
        res.onRight {
            assertEquals(1, it.size)
            assertEquals("", it.first().labels)
        }
    }

    @Test
    fun `when detected object has one label, then correct labels text`() = runTest {
        val sut = createSUT(listOf(TEST_OBJECT_ONE_LABEL))

        val res = sut.call(mockk())

        assertTrue(res.isRight())
        res.onRight {
            assertEquals(1, it.size)
            assertEquals("book - 60%", it.first().labels)
        }

    }

    @Test
    fun `when error, then return error`() = runTest {
        val sut = createSUT(emptyList(), TEST_THROWABLE)

        val res = sut.call(mockk())

        assertTrue(res.isLeft())
        res.onLeft {
            assertEquals(TEST_THROWABLE, it)
        }
    }


    private fun MLKitDetectionRepository.mock(
        result: List<DetectedObject>,
        error: Throwable? = null,
    ) {
        coEvery { detectObjects(any()) } returns (error?.left() ?: result.right())
    }


    companion object {
        private val LABEL_BOOK = DetectedObject.Label("book", 0.6f, 1)
        private val LABEL_PEN = DetectedObject.Label("pen", 0.8f, 2)
        val TEST_THROWABLE = Throwable("test")
        val TEST_OBJECT_MANY_LABELS = DetectedObject(
            Rect(1, 2, 3, 4), 1, mutableListOf(
                LABEL_BOOK,
                LABEL_PEN
            )
        )
        val TEST_OBJECT_ONE_LABEL = DetectedObject(
            Rect(1, 2, 3, 4), 1, mutableListOf(LABEL_BOOK)
        )
        val TEST_OBJECT_NO_LABELS = DetectedObject(
            Rect(1, 2, 3, 4), 1, mutableListOf()
        )

    }

}