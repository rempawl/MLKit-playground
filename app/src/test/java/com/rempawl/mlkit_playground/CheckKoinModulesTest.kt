package com.rempawl.mlkit_playground

import com.rempawl.mlkit_playground.di.appModule
import com.rempawl.test.utils.BaseTestKoinModule
import org.junit.jupiter.api.Test
import org.koin.core.module.Module

class CheckKoinModulesTest() : BaseTestKoinModule() {
    override fun provideModule(): Module = appModule

    @Test
    fun checkDependencies() {
        checkKoinDependencies()
    }
}