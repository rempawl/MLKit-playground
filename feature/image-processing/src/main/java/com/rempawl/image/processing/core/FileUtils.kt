package com.rempawl.image.processing.core

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import arrow.core.Either
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.withContext
import java.io.File

class FileUtils(
    private val dispatchersProvider: DispatchersProvider,
    private val context: Context,
) {
    // todo core-android module
    // todo interface

    suspend fun getTmpCameraFileUriString(): EitherResult<String> =
        getTmpCameraFileUri().map { it.toString() }

    suspend fun getInputImage(uri: String): EitherResult<InputImage> = getInputImage(uri.toUri())

    private suspend fun getTmpCameraFileUri(): EitherResult<Uri> =
        withContext(dispatchersProvider.io) {
            Either.catch {
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
    }
}