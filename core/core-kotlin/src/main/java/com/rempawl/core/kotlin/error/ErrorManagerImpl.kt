/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rempawl.core.kotlin.error

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.rempawl.core.kotlin.extensions.delayFlow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow

class ErrorManagerImpl : ErrorManager {
    private val pendingErrors = Channel<UIError>(
        ErrorManager.SIZE_ERROR_QUEUE,
        BufferOverflow.DROP_OLDEST
    )
    private val removeErrorSignal = Channel<Unit>(Channel.RENDEZVOUS)

    /**
     * A flow of [AppError]s to display in the UI, usually as snackbars. The flow will emit errors
     * sent via [addError]. Once [AppError.duration] has elapsed or [removeCurrentError] is called
     * `Unit.left()` will be emitted to remove the current error.
     */
    override val errors: Flow<Either<Unit, UIError>> = flow {
        pendingErrors.receiveAsFlow().collect { error ->
            emit(error.right())

            // Wait for either a durationMs timeout, or a remove signal (whichever comes first)
            merge(
                delayFlow(error.duration, Unit),
                removeErrorSignal.receiveAsFlow()
            ).firstOrNull()

            // Remove the error
            emit(Unit.left())
        }
    }

    override suspend fun addError(error: UIError) {
        pendingErrors.send(error)
    }

    override suspend fun removeCurrentError() {
        removeErrorSignal.send(Unit)
    }
}