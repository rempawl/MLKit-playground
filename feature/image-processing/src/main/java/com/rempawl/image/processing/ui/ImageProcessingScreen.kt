package com.rempawl.image.processing.ui

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.DetectedObject
import com.rempawl.image.processing.DetectedTextObject
import com.rempawl.image.processing.ImageProcessingState
import com.rempawl.image.processing.ImageProcessingViewModel
import com.rempawl.image.processing.ImageState
import com.rempawl.image.processing.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun ImageProcessingScreen(viewModel: ImageProcessingViewModel = koinViewModel()) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    ImagesScreen(
        state = state,
        processImage = { uri ->
            viewModel.processImage(
                imageUri = uri?.toString().orEmpty(),
                inputImageProvider = { InputImage.fromFilePath(context, uri ?: Uri.EMPTY) }
            )
        }
    )
}

@Composable
private fun ImagesScreen(
    state: ImageProcessingState,
    processImage: (Uri?) -> Unit, // todo mvi and actions
) {
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> processImage(uri) }
    Scaffold(
        topBar = { ToolbarContent() },
        floatingActionButton = { FabContent(pickMedia) },
        snackbarHost = {
            // todo show snackbar
        },
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
    ) {
        ImageProcessingScreen(
            state, modifier = Modifier.padding(it)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ToolbarContent() {
    Surface(shadowElevation = dimensionResource(R.dimen.elevation_toolbar)) {
        TopAppBar(
            title = { Text(stringResource(R.string.title_topbar)) },
        )
    }
}

@Composable
private fun FabContent(pickMedia: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>) {
    FloatingActionButton(
        onClick = {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        content = {
            Icon(
                painter = painterResource(R.drawable.ic_add_photo), contentDescription = null
            )
        }
    )
}

@Composable
private fun ImageProcessingScreen(
    state: ImageProcessingState,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .padding(horizontal = dimensionResource(R.dimen.margin_default))
            .fillMaxSize()
    ) {
        with(state) {
            when {
                isProgressVisible -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                imageState.uri.isNotEmpty() -> ImagesContent(
                    imageState = imageState,
                    detectedObjects = detectedObjects,
                    detectedTextObjects = detectedTextObjects,
                )
            }
        }
    }
}

@Composable
private fun ImagesContent(
    imageState: ImageState,
    detectedObjects: List<DetectedObject>,
    detectedTextObjects: List<DetectedTextObject>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val rectStroke = remember { Stroke(3.0f) }
    val rectColor = remember { Color.Cyan }
    LazyColumn(
        modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            TitleText(title = stringResource(R.string.title_objects_section))
        }
        item {
            ProcessedImage(imageState) { matrix ->
                detectedObjects.forEach { detectedObject ->
                    val scaledRect = matrix.map(detectedObject.rect.toComposeRect())
                    drawOutline(
                        outline = Outline.Rectangle(scaledRect),
                        color = rectColor,
                        style = rectStroke
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = detectedObject.labels,
                        topLeft = scaledRect.topLeft,
                        style = TextStyle(
                            fontSize = 10.sp, color = Color.White, background = rectColor
                        ),
                        size = scaledRect.size
                    )
                }
            }
        }
        item {
            TitleText(title = stringResource(R.string.title_texts_section))
        }
        item {
            ProcessedImage(imageState) { matrix ->
                detectedTextObjects.forEach { detectedTextObject ->
                    val scaledRect = matrix.map(detectedTextObject.rect.toComposeRect())
                    drawOutline(
                        outline = Outline.Rectangle(scaledRect),
                        color = rectColor,
                        style = rectStroke
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcessedImage(
    imageState: ImageState,
    drawBlock: ContentDrawScope.(Matrix) -> Unit,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(imageState.uri).build(),
        contentDescription = null,
        modifier = Modifier
            .animateContentSize()
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawBlock(createScaleMatrix(imageState))
                }
            }
    )
}

@Composable
private fun TitleText(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(12.dp),
        style = MaterialTheme.typography.headlineSmall
    )
}
