package com.rempawl.test.utils

import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.verify.verify


abstract class BaseTestKoinModule() : KoinTest {

    abstract fun provideModule(): Module

    fun checkKoinDependencies() {
        provideModule().verify()
    }
}