package com.rempawl.mlkit_playground.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.Test

class NoMlKitInUiLayerRuleTest {

    private val ruleAssertThat = assertThatRule { NoMlKitInUiLayerRule() }

    @Test
    fun `forbids ML Kit imports in ui package`() {
        val code =
            """
            package com.rempawl.mlkitplayground.ui.camera

            import com.google.mlkit.vision.common.InputImage

            class CameraScreen
            """.trimIndent()

        ruleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(
                line = 3,
                col = 1,
                detail = "Do not import ML Kit APIs (import com.google.mlkit.vision.common.InputImage) directly in the UI layer. Wrap detectors in a domain/data analyzer.",
            )
    }

    @Test
    fun `allows ML Kit imports in data analyzer package`() {
        val code =
            """
            package com.rempawl.mlkitplayground.data.analyzer

            import com.google.mlkit.vision.common.InputImage

            class TextAnalyzer
            """.trimIndent()

        ruleAssertThat(code).hasNoLintViolations()
    }
}