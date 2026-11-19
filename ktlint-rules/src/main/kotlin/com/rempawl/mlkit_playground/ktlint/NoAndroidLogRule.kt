package com.rempawl.mlkit_playground.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class NoAndroidLogRule :
    Rule(
        ruleId = RuleId("mlkit-playground:no-android-log"),
        about = About(
            maintainer = "MLKit-playground",
            repositoryUrl = "https://github.com/rempawl/MLKit-playground",
            issueTrackerUrl = "https://github.com/rempawl/MLKit-playground/issues",
        ),
    ),
    RuleAutocorrectApproveHandler {

    private val logMethods = setOf("Log.v", "Log.d", "Log.i", "Log.w", "Log.e", "Log.wtf")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType == DOT_QUALIFIED_EXPRESSION &&
            logMethods.any { node.text.startsWith("$it(") }
        ) {
            emit(
                node.startOffset,
                "Avoid android.util.Log; remove debug logs or use a structured logger.",
                false,
            )
        }
    }
}
