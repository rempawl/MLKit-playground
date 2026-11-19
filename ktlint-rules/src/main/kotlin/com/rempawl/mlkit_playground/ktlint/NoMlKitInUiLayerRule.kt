package com.rempawl.mlkit_playground.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class NoMlKitInUiLayerRule :
    Rule(
        ruleId = RuleId("mlkit-playground:no-mlkit-in-ui-layer"),
        about = About(
            maintainer = "MLKit-playground",
            repositoryUrl = "https://github.com/rempawl/MLKit-playground",
            issueTrackerUrl = "https://github.com/rempawl/MLKit-playground/issues",
        ),
    ),
    RuleAutocorrectApproveHandler {

    private var isUiPackage = false

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        isUiPackage = false
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        when (node.elementType) {
            PACKAGE_DIRECTIVE -> {
                val packageName = node.text.removePrefix("package").trim()
                isUiPackage = packageName.split(".").any { it == "ui" || it == "presentation" }
            }

            IMPORT_DIRECTIVE -> {
                if (isUiPackage && node.text.contains("com.google.mlkit")) {
                    emit(
                        node.startOffset,
                        "Do not import ML Kit APIs (${node.text.trim()}) directly in the UI layer. Wrap detectors in a domain/data analyzer.",
                        false,
                    )
                }
            }
        }
    }
}
