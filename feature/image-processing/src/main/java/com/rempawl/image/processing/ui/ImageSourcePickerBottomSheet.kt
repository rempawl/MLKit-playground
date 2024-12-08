package com.rempawl.image.processing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rempawl.image.processing.ImageProcessingAction
import com.rempawl.image.processing.ImageProcessingState
import com.rempawl.image.processing.R
import com.rempawl.image.processing.core.FilePickerOption

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ImageSourcePickerBottomSheet(
    submitAction: (ImageProcessingAction) -> Unit,
    state: ImageProcessingState,
) {
    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = { submitAction(ImageProcessingAction.HideImageSourcePicker) },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        ImageSourcePickerBottomSheet(
            pickerOptions = state.sourcePickerOptions,
            submitAction = { submitAction(ImageProcessingAction.FilePickerOptionSelected(it)) }
        )
    }
}


@Composable
private fun ImageSourcePickerBottomSheet(
    pickerOptions: List<FilePickerOption>,
    submitAction: (FilePickerOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Select image source", // todo
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        HeightSpacer()
        pickerOptions.forEach { imagePickerOption ->
            imagePickerOption.Content(submitAction)
            HeightSpacer()
        }
    }
}

private fun FilePickerOption.getText() = when (this) {
    FilePickerOption.Camera -> "Camera" //todo string resource
    FilePickerOption.Gallery -> "Gallery"
}

@Composable
private fun FilePickerOption.Content(submitAction: (FilePickerOption) -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        onClick = { submitAction(this) }
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = this@Content.getIconPainter(),
            contentDescription = null
        )
        Spacer(Modifier.width(12.dp))
        Text(text = getText())
    }

}

@Composable
private fun FilePickerOption.getIconPainter() = when (this) {
    FilePickerOption.Camera -> painterResource(R.drawable.ic_camera)
    FilePickerOption.Gallery -> painterResource(R.drawable.ic_gallery)
}
