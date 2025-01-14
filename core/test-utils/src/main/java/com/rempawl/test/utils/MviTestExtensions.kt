package com.rempawl.test.utils

import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.rempawl.core.viewmodel.mvi.Action
import com.rempawl.core.viewmodel.mvi.BaseMVIViewModel
import com.rempawl.core.viewmodel.mvi.Effect

class MVITestContext<TurbineContext, ACTION : Action>(
    private val mviComponent: BaseMVIViewModel<*, ACTION, *>,
    turbineTestContext: TurbineTestContext<TurbineContext>
) : TurbineTestContext<TurbineContext> by turbineTestContext {

    fun submitAction(action: ACTION) = mviComponent.submitAction(action)
}

suspend fun <State, A : Action> BaseMVIViewModel<State, A, *>.testState(
    testBlock: suspend MVITestContext<State, A>.() -> Unit
) {
    this.state.test {
        testBlock(
            MVITestContext(this@testState, this)
        )
    }
}

suspend fun <EFFECT : Effect, A : Action> BaseMVIViewModel<*, A, EFFECT>.testEffects(
    testBlock: suspend MVITestContext<EFFECT, A>.() -> Unit
) {
    this.effects.test {
        MVITestContext(this@testEffects, this).testBlock()
    }
}