package com.rempawl.snackbar

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.rempawl.core.kotlin.error.UIError

@Composable
fun AppSnackbarHost(
    snackbarHostState: SnackbarHostState,
    error: UIError?,
) {
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(error.message) }
    }
    SnackbarHost(hostState = snackbarHostState)
}
