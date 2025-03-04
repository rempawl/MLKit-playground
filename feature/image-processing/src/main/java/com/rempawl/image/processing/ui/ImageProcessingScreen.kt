package com.rempawl.image.processing.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.rempawl.core.ui.bottomsheet.ImageSourcePickerBottomSheet
import com.rempawl.core.ui.bottomsheet.toPickVisualMediaRequest
import com.rempawl.core.ui.createScaleMatrix
import com.rempawl.core.ui.snackbar.AppSnackbarHost
import com.rempawl.core.ui.toComposeRect
import com.rempawl.image.processing.R
import com.rempawl.image.processing.model.DetectedObject
import com.rempawl.image.processing.model.DetectedTextObject
import com.rempawl.image.processing.viewmodel.ImageProcessingAction
import com.rempawl.image.processing.viewmodel.ImageProcessingAction.ImageSourcePickerOptionSelected
import com.rempawl.image.processing.viewmodel.ImageProcessingEffect
import com.rempawl.image.processing.viewmodel.ImageProcessingState
import com.rempawl.image.processing.viewmodel.ImageProcessingViewModel
import com.rempawl.image.processing.viewmodel.ImageState
import com.rempawl.mlkit_playground.ui.theme.MlKitplaygroundTheme
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.navigation.koinNavViewModel


@Destination<RootGraph>(start = true)
@Composable
fun ImageProcessingScreen(
    viewModel: ImageProcessingViewModel = koinNavViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.submitAction(ImageProcessingAction.LifecycleStopped)
    }
    EffectsObserver(
        effectsProvider = { viewModel.effects },
        submitAction = { viewModel.submitAction(it) }
    )
    ImagesProcessingScreen(
        state = state,
        submitAction = { viewModel.submitAction(it) },
    )
}

@Composable
private fun ImagesProcessingScreen(
    state: ImageProcessingState,
    submitAction: (ImageProcessingAction) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { ToolbarContent() },
        floatingActionButton = {
            FabContent {
                submitAction(ImageProcessingAction.SelectImageFabClicked)
            }
        },
        snackbarHost = {
            AppSnackbarHost(snackbarHostState, state.error)
        },
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
    ) { paddingValues ->
        ImageProcessingScreenContent(
            state, modifier = Modifier.padding(paddingValues)
        )
        if (state.isSourcePickerVisible) {
            ImageSourcePickerBottomSheet(
                pickerOptions = state.sourcePickerOptions,
                onPickerOptionSelected = { submitAction(ImageSourcePickerOptionSelected(it)) },
                onDismiss = { submitAction(ImageProcessingAction.HideImageSourcePicker) }
            )
        }
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
private fun FabContent(pickMedia: () -> Unit) {
    FloatingActionButton(
        onClick = { pickMedia() },
        content = {
            Icon(
                painter = painterResource(R.drawable.ic_add_photo),
                contentDescription = null
            )
        })
}

@Composable
private fun ImageProcessingScreenContent(
    state: ImageProcessingState,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .padding(horizontal = dimensionResource(R.dimen.margin_default))
            .fillMaxSize()
    ) {
        with(state) {
            if (isProgressVisible) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                AnimatedVisibility(
                    imageState.uri.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(), // todo take from compositionLocal
                    exit = slideOutVertically()
                ) {
                    ImagesContent(
                        imageState = imageState,
                        detectedObjects = detectedObjects,
                        detectedTextObjects = detectedTextObjects,
                    )
                }
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
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()
    val rectStroke = remember { Stroke(3.0f) }
    val rectColor = remember { Color.Cyan } // todo add to theme
    val textColor = remember { Color.Black } // todo add to theme
    val imageRequest = remember(imageState.uri) {
        ImageRequest.Builder(context).data(imageState.uri).build()
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            TitleText(title = stringResource(R.string.title_objects_section))
        }
        item {
            ProcessedImage(imageState, imageRequest) { matrix ->
                detectedObjects.fastForEach { detectedObject ->
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
                            fontSize = 10.sp, color = textColor, background = rectColor
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
            ProcessedImage(imageState, imageRequest) { matrix ->
                detectedTextObjects.fastForEach { detectedTextObject ->
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
    imageRequest: ImageRequest,
    drawBlock: ContentDrawScope.(Matrix) -> Unit,
) {
    AsyncImage(
        model = imageRequest,
        contentDescription = null,
        modifier = Modifier
            .animateContentSize()
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawBlock(
                        createScaleMatrix(
                            width = imageState.width.toFloat(),
                            height = imageState.height.toFloat()
                        )
                    )
                }
            }
    )
}

@Composable
private fun TitleText(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(12.dp),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EffectsObserver(
    effectsProvider: () -> SharedFlow<ImageProcessingEffect>,
    submitAction: (ImageProcessingAction) -> Unit,
) {
    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        submitAction(
            ImageProcessingAction.GalleryImagePicked(imageUri = uri?.toString().orEmpty())
        )
    }
    val cameraLauncher = rememberLauncherForActivityResult(TakePicture()) {
        submitAction(ImageProcessingAction.PictureTaken(isImageSaved = it))
    }
    LaunchedEffect(Unit) {
        effectsProvider().collectLatest {
            when (it) {
                is ImageProcessingEffect.TakePicture -> cameraLauncher.launch(it.uri.toUri())

                is ImageProcessingEffect.OpenGallery -> pickMedia.launch(
                    it.pickerOption.toPickVisualMediaRequest()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageProcessingScreenPreview(
    @PreviewParameter(ImageProcessingPreviewProvider::class) state: ImageProcessingState
) {
    MlKitplaygroundTheme {
        ImagesProcessingScreen(state) {}
    }
}
