package com.rempawl.image.processing

import io.mockk.MockKVerificationScope
import io.mockk.coVerify
import io.mockk.verify

fun coVerifyOnce(block: suspend MockKVerificationScope.() -> Unit) {
    coVerify(exactly = 1) { block() }
}

fun coVerifyNever(block: suspend MockKVerificationScope.() -> Unit) {
    coVerify(exactly = 0) { block() }
}

fun verifyOnce(block: MockKVerificationScope.() -> Unit) {
    verify(exactly = 1) { block() }
}

fun verifyNever(block: MockKVerificationScope.() -> Unit) {
    verify(exactly = 0) { block() }
}