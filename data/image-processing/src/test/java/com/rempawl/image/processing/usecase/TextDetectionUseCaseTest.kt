package com.rempawl.image.processing.usecase

import android.graphics.Rect
import arrow.core.left
import arrow.core.right
import com.rempawl.image.processing.model.DetectedTextObject
import com.rempawl.image.processing.repository.MLKitDetectionRepositoryImpl
import com.rempawl.test.utils.BaseCoroutineTest
import com.rempawl.test.utils.coVerifyNever
import com.rempawl.test.utils.coVerifyOnce
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TextDetectionUseCaseTest : BaseCoroutineTest() {

    private val repository = mockk<MLKitDetectionRepositoryImpl>()

    private fun createSUT(): TextDetectionUseCase {
        return TextDetectionUseCase(repository)
    }

    @Test
    fun `when use case called, then repository method called once`() = runTest {
        repository.mock()

        createSUT().run {
            coVerifyNever { repository.detectText(any()) }

            call(mockk())

            coVerifyOnce { repository.detectText(any()) }
        }
    }

    @Test
    fun `when no texts detected, then return empty list`() = runTest {
        repository.mock(emptyList())
        createSUT().run {

            val res = this.call(mockk())

            assertTrue(res.isRight())
            res.onRight {
                assertEquals(0, it.size)
            }
        }
    }

    @Test
    fun `when one text detected, then return list with one item`() = runTest {
        repository.mock(
            listOf(
                TextBlockWrapper(TEST_RECT)
            )
        )
        val sut = createSUT()

        val res = sut.call(mockk())

        assertTrue(res.isRight())
        res.onRight {
            assertIs<List<DetectedTextObject>>(it)
            assertEquals(1, it.size)
        }
    }

    @Test
    fun `when repository returns empty block, then empty list returned`() = runTest {
        repository.mock(
            listOf(
                TextBlockWrapper(null)
            )
        )
        val sut = createSUT()

        val res = sut.call(mockk())

        assertTrue(res.isRight())
        res.onRight {
            assertEquals(0, it.size)
        }
    }

    @Test
    fun `when error, then return left of error`() = runTest {
        repository.mock(error = TEST_THROWABLE)
        val sut = createSUT()

        val res = sut.call(mockk())

        assertTrue(res.isLeft())
        res.onLeft {
            assertEquals(TEST_THROWABLE, it)
        }
    }

    private fun MLKitDetectionRepositoryImpl.mock(
        result: List<TextBlockWrapper> = emptyList(),
        error: Throwable? = null,
    ) {
        coEvery { detectText(any()) } returns (error?.left() ?: result.right())
    }

    companion object {
        val TEST_RECT = Rect(1, 2, 3, 4)
        val TEST_THROWABLE = Throwable("error")
    }
}