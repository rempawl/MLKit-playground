package com.rempawl.mlkit_playground

import android.content.Context
import com.rempawl.core.android.DefaultErrorMessageProvider
import com.rempawl.mlkit_playground.di.appModule
import com.rempawl.test.utils.BaseTestKoinModule
import org.junit.jupiter.api.Test
import org.koin.core.module.Module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify

class CheckKoinModulesTest() : BaseTestKoinModule() {
    override fun provideModule(): Module = appModule

    @Test
    fun checkDependencies() {
        provideModule().verify(
            injections = injectedParameters(
                definition<DefaultErrorMessageProvider>(Context::class),
            ),
        )
    }
}