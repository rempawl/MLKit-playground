package com.rempawl.core.viewmodel.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.rempawl.core.kotlin.error.ErrorManager
import com.rempawl.core.kotlin.error.UIError
import com.rempawl.core.kotlin.progress.ProgressSemaphore
import com.rempawl.core.kotlin.progress.watchProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Base class for MVI ViewModels.
 *
 * This class provides a foundation for implementing the Model-View-Intent (MVI)
 * architecture pattern in project viewmodels. It manages state, side effects,
 * and error handling.
 *
 * @param <STATE> The type of the UI state.
 * @param <ACTION> The type of the user action.
 * @param <EFFECT> The type of the side effect.
 */
abstract class BaseMVIViewModel<STATE, ACTION : Action, EFFECT : Effect>(
    private val errorManager: ErrorManager,
    private val progressSemaphore: ProgressSemaphore,
    initialState: STATE,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.onSubscription {
        errorManager.errors.onEach { onError(it) }.launchIn(viewModelScope)
        progressSemaphore.hasProgress.onEach { onProgressChange(it) }.launchIn(viewModelScope)
        viewModelScope.launch { doOnStateSubscription().invoke() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), initialState)

    protected val currentState get() = _state.value

    private val _effects = MutableSharedFlow<EFFECT>()
    val effects: SharedFlow<EFFECT> = _effects.asSharedFlow()

    fun submitAction(action: ACTION) {
        viewModelScope.launch {
            handleActions(action)
        }
    }

    /**
     * Adds an error to the error manager.
     * @param error The UI error to be added.
     */
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

    protected fun <T> Flow<T>.watchProgress(): Flow<T> = watchProgress(progressSemaphore)

    protected suspend fun <T> withProgress(
        block: suspend () -> T,
    ): T {
        progressSemaphore.addProgress()
        try {
            return block()
        } finally {
            progressSemaphore.removeProgress()
        }
    }

    /**
     * Handles actions submitted to the ViewModel.
     *
     * This method should be overridden by subclasses to implement the core logic
     * of processing actions and updating the state.
     *
     * @param action The action to be handled.
     */
    protected abstract suspend fun handleActions(action: ACTION)

    /**
     * Handles progress updates reported by the [ProgressSemaphore].
     * This method should be overridden by subclasses to implement progress handling
     * logic, such as updating the state to change visibility of progress.
     *
     * @param hasProgress The current progress state.
     */
    protected abstract suspend fun onProgressChange(hasProgress: Boolean)

    /**
     * Handles errors reported by the error manager.
     *
     * This method should be overridden by subclasses to implement error handling
     * logic, such as updating the state to display an error message.
     *
     * @param appError The application error to be handled.
     * @param state    The current state of the ViewModel.
     * @return The new state of the ViewModel after handling the error.
     */
    protected abstract fun handleError(appError: Either<Unit, UIError>, state: STATE): STATE

    /**
     * Performs actions when the state flow is subscribed to.
     *
     * This method can be overridden by subclasses to perform initialization
     * or setup tasks when the state flow is first observed.
     *
     * @return A suspend function that will be executed when the state flow is subscribed to.
     */
    protected open fun doOnStateSubscription(): suspend () -> Unit = { }

    private fun onError(error: Either<Unit, UIError>) {
        _state.update { currentValue -> handleError(appError = error, state = currentValue) }
    }
}