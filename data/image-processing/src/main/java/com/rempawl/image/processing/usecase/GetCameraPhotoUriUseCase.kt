package com.rempawl.image.processing.usecase

import com.rempawl.core.kotlin.extensions.EitherResult
import com.rempawl.core.kotlin.usecase.ResultUseCase
import com.rempawl.image.processing.repository.ImageProcessingRepository

class GetCameraPhotoUriUseCase internal constructor(
    private val repository: ImageProcessingRepository
) : ResultUseCase<Unit, String> {
    override suspend fun call(param: Unit): EitherResult<String> =
        repository.getTmpCameraFileUriString()
}