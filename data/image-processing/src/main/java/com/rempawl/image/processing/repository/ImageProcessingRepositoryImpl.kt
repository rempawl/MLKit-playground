package com.rempawl.image.processing.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import arrow.core.Either
import com.google.mlkit.vision.common.InputImage
import com.rempawl.core.kotlin.dispatcher.DispatchersProvider
import com.rempawl.core.kotlin.extensions.EitherResult
import kotlinx.coroutines.withContext
import java.io.File

internal class ImageProcessingRepositoryImpl(
    private val dispatchersProvider: DispatchersProvider,
    private val context: Context,
) : ImageProcessingRepository {

    /**
     * Gets the URI for a temporary camera file.
     *
     * This method creates a temporary file in the app's cache directory
     * and returns its URI.
     *
     * @return An `EitherResult` containing the URI if successful,
     * or an error if the operation fails.
     */
    override suspend fun getTmpCameraFileUriString(): EitherResult<String> =
        getTmpCameraFileUri().map { it.toString() }

    override suspend fun getInputImage(uri: String): EitherResult<InputImage> =
        getInputImage(uri.toUri())

    private suspend fun getTmpCameraFileUri(): EitherResult<Uri> =
        withContext(dispatchersProvider.io) {
            Either.catch {
                // todo file provider in core-android
                val cacheDir = File(context.cacheDir, IMAGES_CACHE_DIR)
                if (!cacheDir.exists())
                    cacheDir.mkdir()
                val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir)
                    .apply { createNewFile() }

                FileProvider.getUriForFile(context, context.packageName + ".provider", tmpFile)
            }
        }

    private suspend fun getInputImage(uri: Uri): EitherResult<InputImage> =
        withContext(dispatchersProvider.io) {
            Either.catch {
                InputImage.fromFilePath(context, uri)
            }
        }

    companion object {
        const val IMAGES_CACHE_DIR = "images"
        // todo injected app constant via constantsProvider interface?
    }
}