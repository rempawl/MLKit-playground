package com.rempawl.mlkit_playground.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.CanvasProvider
import com.rempawl.image.processing.ImageProcessingState
import com.rempawl.image.processing.ImageProcessingViewModel
import com.rempawl.image.processing.PaintProvider
import com.rempawl.image.processing.core.DispatchersProvider
import com.rempawl.mlkit_playground.R
import com.rempawl.mlkit_playground.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

// todo compose ui tests
// todo viewbinding ui tests
// todo compare performance between compose and viewbinding
class MainActivity : ComponentActivity() {

    // todo show bottomsheet with camera or gallery picker
    private val mediaPicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { processImage(it) } ?: showError()
        }
    private val viewModel: ImageProcessingViewModel by viewModel<ImageProcessingViewModel>()
    private val canvasProvider by inject<CanvasProvider>()
    private val paintProvider by inject<PaintProvider>()
    private val dispatchersProvider by inject<DispatchersProvider>()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        this.setContentView(binding.root)

        setupBinding()
        setupObservers()
    }

    private fun setupBinding() = with(binding) {
        imageObjects.setupImageView()
        imageText.setupImageView()

        fabSelectImage.setOnClickListener {
            mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }


    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    viewModel.state.collectLatest { imageProcessingState ->
                        imageProcessingState.handleStateChange()
                    }
                }
            }
        }
    }

    private fun ImageProcessingState.handleStateChange() {
        if (showError) showError()
        binding.run {
            progress.isVisible = isProgressVisible
        }
        if (imageUri.isNotEmpty()) {
            binding.run {
                lifecycleScope.launch {
                    setupImages(this@handleStateChange)
                    titleDetectedObjects.showText()
                    titleDetectedTexts.showText()
                }
            }
        } else {
            binding.run {
                imageObjects.isVisible = false
                imageText.isVisible = false
                titleDetectedObjects.isVisible = false
                titleDetectedTexts.isVisible = false
            }
        }
    }

    private fun TextView.showText() =
        post {
            alpha = 0f
            isVisible = true
            animate().alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .start()
        }


    private suspend fun setupImages(
        imageProcessingState: ImageProcessingState,
    ) {
        val bitmap = imageProcessingState.loadBitmap()

        binding.imageText.drawObjects({ copyBitmap(bitmap) }
        ) {
            imageProcessingState.detectedTextObjects.forEach {
                drawRect(it.rect, paintProvider.getObjectPaint())
            }
        }
        binding.imageObjects.drawObjects({ copyBitmap(bitmap) }) {
            imageProcessingState.detectedObjects.forEach {
                drawRect(it.rect, paintProvider.getObjectPaint())
                drawText(
                    it.labels,
                    it.startX,
                    it.startY,
                    paintProvider.getTextPaint(
                        fontSize = resources.getDimension(
                            R.dimen.font_size_object_detection
                        )
                    )
                )
            }
        }
        bitmap.recycle()
    }

    private suspend fun copyBitmap(bitmap: Bitmap): Bitmap =
        withContext(dispatchersProvider.default) {
            bitmap.copy(
                Bitmap.Config.ARGB_8888,
                true
            )
        }

    private suspend fun ImageProcessingState.loadBitmap() =
        withContext(dispatchersProvider.default) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    contentResolver,
                    imageUri.toUri()
                )
            )
        }


    private suspend fun ImageView.drawObjects(
        bitmapProvider: suspend () -> Bitmap,
        drawBlock: Canvas.() -> Unit,
    ) {
        post {
            scaleX = 0.1f
            scaleY = 0.1f
            alpha = 0f
            isVisible = true
        }
        val bitmap = bitmapProvider()

        canvasProvider.use(bitmap)
        { drawBlock() }
        post {
            setImageBitmap(bitmap)
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(ANIMATION_DURATION)
                .start()
        }
    }


    private fun showError() {
        Snackbar.make(binding.root.rootView, "An error occurred", Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun processImage(uri: Uri) {
        viewModel.processImage(
            inputImage = InputImage.fromFilePath(this@MainActivity, uri),
            imageUri = uri.toString()
        )
    }

    companion object {
        private const val ANIMATION_DURATION = 700L
    }
}