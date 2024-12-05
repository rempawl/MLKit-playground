package com.rempawl.image.processing

import io.mockk.MockKVerificationScope
import io.mockk.coVerify

fun coVerifyOnce(block: suspend MockKVerificationScope.() -> Unit) {
    coVerify(exactly = 1) { block() }
}

fun coVerifyNever(block: suspend MockKVerificationScope.() -> Unit) {
    coVerify(exactly = 0) { block() }
}