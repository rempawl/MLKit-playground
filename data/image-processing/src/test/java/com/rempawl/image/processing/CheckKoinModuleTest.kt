package com.rempawl.image.processing

import com.rempawl.image.processing.di.dataImageProcessingModule
import com.rempawl.test.utils.BaseTestKoinModule
import org.junit.jupiter.api.Test
import org.koin.core.module.Module

class CheckKoinModuleTest() : BaseTestKoinModule() {
    override fun provideModule(): Module = dataImageProcessingModule

    @Test
    fun checkDependencies() {
        checkKoinDependencies()
    }
}