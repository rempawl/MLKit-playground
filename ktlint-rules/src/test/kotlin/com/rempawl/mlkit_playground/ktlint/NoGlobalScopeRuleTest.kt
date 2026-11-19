package com.rempawl.mlkit_playground.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.Test

class NoGlobalScopeRuleTest {

    private val ruleAssertThat = assertThatRule { NoGlobalScopeRule() }

    @Test
    fun `forbids GlobalScope`() {
        val code =
            """
            fun test() {
                GlobalScope.launch { }
            }
            """.trimIndent()

        ruleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(
                line = 2,
                col = 5,
                detail = "Avoid GlobalScope; use viewModelScope, lifecycleScope, or an injected CoroutineScope instead.",
            )
    }

    @Test
    fun `allows viewModelScope`() {
        val code =
            """
            fun test() {
                viewModelScope.launch { }
            }
            """.trimIndent()

        ruleAssertThat(code).hasNoLintViolations()
    }
}
