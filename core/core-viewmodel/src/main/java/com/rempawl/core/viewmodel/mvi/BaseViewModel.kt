package com.rempawl.core.viewmodel.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.rempawl.core.kotlin.error.AppError
import com.rempawl.core.kotlin.error.ErrorManager
import com.rempawl.core.kotlin.error.UIError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


abstract class BaseMVIViewModel<STATE, ACTION : Action, EFFECT : Effect>(
    private val errorManager: ErrorManager, initialState: STATE,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state
        .asStateFlow()
        .onSubscription {
            errorManager.errors.onEach { onError(it) }.launchIn(viewModelScope)
            doOnStateSubscription().invoke()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), initialState)

    protected val currentState get() = _state.value

    private val _effects = MutableSharedFlow<EFFECT>()
    val effects: SharedFlow<EFFECT> = _effects.asSharedFlow()

    fun submitAction(action: ACTION) {
        viewModelScope.launch {
            handleActions(action)
        }
    }

    protected fun addError(error: UIError) {
        viewModelScope.launch {
            errorManager.addError(error)
        }
    }

    protected fun setState(reducer: STATE.() -> STATE) {
        _state.update { currentValue -> currentValue.reducer() }
    }

    protected suspend fun setEffect(reducer: STATE.() -> EFFECT) {
        _effects.emit(currentState.reducer())
    }

    protected abstract suspend fun handleActions(action: ACTION)
    protected abstract fun handleError(appError: Either<Unit, AppError>, state: STATE): STATE
    protected open fun doOnStateSubscription(): suspend () -> Unit = { }


    private fun onError(error: Either<Unit, AppError>) {
        _state.update { currentValue -> handleError(appError = error, state = currentValue) }
    }

}