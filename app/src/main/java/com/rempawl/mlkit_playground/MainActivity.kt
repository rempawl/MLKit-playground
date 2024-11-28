package com.rempawl.mlkit_playground

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.common.InputImage
import com.rempawl.mlkit_playground.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


object PaintProvider {

    private val paint by lazy {
        Paint()
    }

    fun customize(decorator: Paint.() -> Unit): Paint {
        return paint.apply(decorator)
    }

    fun createObjectPaint(): Paint =
        customize {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.SQUARE
            strokeWidth = 5.0f
            color = Color.CYAN
        }

    fun createTextPaint(resources: Resources): Paint = customize {
        style = Paint.Style.FILL
        color = Color.CYAN
        textSize = resources.getDimension(R.dimen.font_size_object_detection)
    }
}


// todo compose ui tests
// todo viewbinding ui tests
class MainActivity : ComponentActivity() {

    // todo show bottomsheet with camera or gallery picker
    private val mediaPicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { processImage(it) } ?: showError()
        }

    private val viewModel: ImageProcessingViewModel by viewModel<ImageProcessingViewModel>()
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
        binding.progress.isVisible = isProgressVisible
        binding.progressOverlay.isVisible = isProgressVisible
        if (imageUri.isNotEmpty()) {
            binding.titleDetectedObjects.isVisible = true
            drawDetectedObjects(detectedObjects, imageUri.toUri())
            binding.titleDetectedTexts.isVisible = true
            drawDetectedTextObjects(detectedTextObjects, imageUri.toUri())
        }
    }

    private fun drawDetectedTextObjects(
        detectedTextObjects: List<DetectedTextObject>,
        imageUri: Uri
    ) = with(binding.imageText) {
        setImageURI(imageUri)
        // todo check performance difference when not returning
        if (detectedTextObjects.isEmpty()) return@with

        val (bitmap, canvas) = createBitmapAndCanvas()

        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.SQUARE
            strokeWidth = 5.0f
            color = Color.CYAN
        }

        detectedTextObjects.forEach {
            canvas.drawRect(it.rect, paint)
        }
        foreground = bitmap.toDrawable(resources)
    }

    private fun drawDetectedObjects(
        detectedObjects: List<DetectedObject>,
        imageUri: Uri
    ) = with(binding.imageObjects) {
        setImageURI(imageUri)
        if (detectedObjects.isEmpty()) return@with

        val (bitmap, canvas) = createBitmapAndCanvas()

        // todo check performance difference when reusing paint
        val objectPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.SQUARE
            strokeWidth = 5.0f
            color = Color.CYAN
        }

        val textPaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.CYAN
            textSize = resources.getDimension(R.dimen.font_size_object_detection)
        }

        detectedObjects.forEach {
            canvas.drawRect(it.rect, objectPaint)
            // todo break text when overlaps rect
            canvas.drawText(
                it.labels,
                it.startX,
                it.startY,
                textPaint
            )
        }
        foreground = bitmap.toDrawable(resources)
    }

    private fun ImageView.createBitmapAndCanvas(): Pair<Bitmap, Canvas> {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        // todo check performance difference when reusing canvas
        val canvas = Canvas().apply { setBitmap(bitmap) }
        return Pair(bitmap, canvas)
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