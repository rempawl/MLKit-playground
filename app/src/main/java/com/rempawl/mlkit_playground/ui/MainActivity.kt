package com.rempawl.mlkit_playground.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.common.InputImage
import com.rempawl.image.processing.CanvasProvider
import com.rempawl.image.processing.DetectedObject
import com.rempawl.image.processing.DetectedTextObject
import com.rempawl.image.processing.ImageProcessingState
import com.rempawl.image.processing.ImageProcessingViewModel
import com.rempawl.image.processing.PaintProvider
import com.rempawl.mlkit_playground.R
import com.rempawl.mlkit_playground.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

// todo compose ui tests
// todo viewbinding ui tests
class MainActivity : ComponentActivity() {

    // todo show bottomsheet with camera or gallery picker
    private val mediaPicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { processImage(it) } ?: showError()
        }
    private val viewModel: ImageProcessingViewModel by viewModel<ImageProcessingViewModel>()
    private val canvasProvider by inject<CanvasProvider>()
    private val paintProvider by inject<PaintProvider>()
    private lateinit var binding: ActivityMainBinding // todo compose

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        this.setContentView(binding.root)

        // todo make in compose with painting on overlay
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

    private fun ImageView.setupImageView() {
        adjustViewBounds = true
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
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
                imageText.setImageURI(imageUri.toUri())
                imageObjects.setImageURI(imageUri.toUri())
                titleDetectedObjects.isVisible = true
                titleDetectedTexts.isVisible = true
            }
            if (detectedObjects.isNotEmpty()) drawDetectedObjects(detectedObjects)
            if (detectedTextObjects.isNotEmpty()) drawDetectedTextObjects(detectedTextObjects)
        }
    }

    private fun drawDetectedTextObjects(detectedTextObjects: List<DetectedTextObject>) =
        with(binding.imageText) {
            val bitmap = copyBitmapFromDrawable()

            detectedTextObjects.forEach {
                canvasProvider.use(bitmap) {
                    drawRect(it.rect, paintProvider.getObjectPaint())
                }
            }
            setImageBitmap(bitmap)
        }

    private fun drawDetectedObjects(detectedObjects: List<DetectedObject>) =
        with(binding.imageObjects) {
            val bitmap = copyBitmapFromDrawable()

            detectedObjects.forEach {
                canvasProvider.use(bitmap) {
                    drawRect(it.rect, paintProvider.getObjectPaint())
                    // todo break text when overlaps rect
                    drawText(
                        it.labels, it.startX, it.startY, paintProvider.getTextPaint(
                            fontSize = resources.getDimension(R.dimen.font_size_object_detection)
                        )
                    )
                }
            }
            setImageBitmap(bitmap)
        }

    private fun ImageView.copyBitmapFromDrawable() = drawable.toBitmap().run {
        val copy = copy(Bitmap.Config.ARGB_8888, true)
        recycle()
        copy
    }


    private fun showError() {
        // todo show snackbar
    }

    private fun processImage(uri: Uri) {
        viewModel.processImage(
            inputImageProvider = { InputImage.fromFilePath(this@MainActivity, uri) },
            imageUri = uri.toString()
        )
    }
}