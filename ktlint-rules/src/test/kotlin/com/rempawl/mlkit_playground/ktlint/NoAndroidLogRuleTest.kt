package com.rempawl.mlkit_playground.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.Test

class NoAndroidLogRuleTest {

    private val ruleAssertThat = assertThatRule { NoAndroidLogRule() }

    @Test
    fun `forbids Log call`() {
        val code =
            """
            fun test() {
                Log.d("Tag", "message")
            }
            """.trimIndent()

        ruleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(
                line = 2,
                col = 5,
                detail = "Avoid android.util.Log; remove debug logs or use a structured logger.",
            )
    }

    @Test
    fun `allows custom logger`() {
        val code =
            """
            fun test() {
                MyLogger.debug("message")
            }
            """.trimIndent()

        ruleAssertThat(code).hasNoLintViolations()
    }
}
