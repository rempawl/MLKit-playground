package com.rempawl.core.ui.bottomsheet

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rempawl.core.ui.R
import com.rempawl.core.ui.spacer.HeightSpacer
import com.rempawl.mlkit_playground.ui.theme.MlKitplaygroundTheme

/**
 * A bottom sheet that allows the user to select an image source.
 *
 * This composable displays a modal bottom sheet with options for selecting an image source,
 * such as the camera or gallery. When the user selects an option, the `onPickerOptionSelected`
 * callback is invoked with the chosen [ImageSourcePickerOption]. The bottom sheet can be
 * dismissed by calling the `onDismiss` callback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourcePickerBottomSheet(
    pickerOptions: List<ImageSourcePickerOption>,
    onPickerOptionSelected: (ImageSourcePickerOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        ImageSourcePicker(
            pickerOptions = pickerOptions, submitAction = onPickerOptionSelected
        )
    }
}

@Composable
private fun ImageSourcePicker(
    pickerOptions: List<ImageSourcePickerOption>,
    submitAction: (ImageSourcePickerOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.image_source_picker_title),
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

@Composable
private fun ImageSourcePickerOption.getText() = when (this) {
    ImageSourcePickerOption.CAMERA -> stringResource(R.string.image_source_picker_camera_label)
    ImageSourcePickerOption.GALLERY -> stringResource(R.string.image_source_picker_gallery_label)
}

@Composable
private fun ImageSourcePickerOption.Content(submitAction: (ImageSourcePickerOption) -> Unit) {
    Button(modifier = Modifier
        .fillMaxWidth() // todo landscape
        .height(48.dp), onClick = { submitAction(this) }) {
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
private fun ImageSourcePickerOption.getIconPainter() = when (this) {
    ImageSourcePickerOption.CAMERA -> painterResource(R.drawable.ic_camera)
    ImageSourcePickerOption.GALLERY -> painterResource(R.drawable.ic_gallery)
}

@Preview(showBackground = true)
@Composable
fun ImageSourcePickerPreview() {
    MlKitplaygroundTheme {
        ImageSourcePicker(
            listOf(
                ImageSourcePickerOption.GALLERY, ImageSourcePickerOption.CAMERA
            ),
            {},
        )
    }
}