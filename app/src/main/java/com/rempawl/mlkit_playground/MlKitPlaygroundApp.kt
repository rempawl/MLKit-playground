package com.rempawl.mlkit_playground

import android.app.Application
import com.rempawl.mlkit_playground.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MlKitPlaygroundApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MlKitPlaygroundApp)
            modules(appModule)
        }
    }
}