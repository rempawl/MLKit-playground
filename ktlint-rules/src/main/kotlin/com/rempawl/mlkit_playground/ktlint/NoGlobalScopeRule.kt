package com.rempawl.mlkit_playground.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class NoGlobalScopeRule :
    Rule(
        ruleId = RuleId("mlkit-playground:no-global-scope"),
        about = About(
            maintainer = "MLKit-playground",
            repositoryUrl = "https://github.com/rempawl/MLKit-playground",
            issueTrackerUrl = "https://github.com/rempawl/MLKit-playground/issues",
        ),
    ),
    RuleAutocorrectApproveHandler {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType == REFERENCE_EXPRESSION && node.text == "GlobalScope") {
            emit(
                node.startOffset,
                "Avoid GlobalScope; use viewModelScope, lifecycleScope, or an injected CoroutineScope instead.",
                false,
            )
        }
    }
}