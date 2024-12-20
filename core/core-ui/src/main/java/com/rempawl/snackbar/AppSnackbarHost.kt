package com.rempawl.snackbar

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.rempawl.core.ui.R

@Composable
fun AppSnackbarHost(
    snackbarHostState: SnackbarHostState,
    showError: Boolean // todo AppError interface subclasses
) {
    val message = stringResource(R.string.error_generic) // todo getErrorMessage handler
    LaunchedEffect(showError) {
        if (showError) snackbarHostState.showSnackbar(message)
    }
    SnackbarHost(hostState = snackbarHostState)
}
