package com.rempawl.mlkit_playground

import com.rempawl.mlkit_playground.di.appModule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class CheckKoinModulesTest : KoinTest {

    @Test
    fun checkAllModules() {
        appModule.verify()
    }
}