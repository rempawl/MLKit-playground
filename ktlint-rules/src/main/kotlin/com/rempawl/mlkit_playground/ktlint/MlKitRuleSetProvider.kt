package com.rempawl.mlkit_playground.ktlint

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId

class MlKitRuleSetProvider : RuleSetProviderV3(RuleSetId("mlkit-playground")) {
    override fun getRuleProviders(): Set<RuleProvider> = setOf(
        RuleProvider { NoMlKitInUiLayerRule() },
        RuleProvider { NoGlobalScopeRule() },
        RuleProvider { NoAndroidLogRule() },
    )
}