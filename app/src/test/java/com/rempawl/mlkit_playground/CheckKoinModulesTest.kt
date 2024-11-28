package com.rempawl.mlkit_playground

import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class CheckKoinModulesTest : KoinTest {

    @Test
    fun checkAllModules() {
        appModule.verify()
    }
}