package com.rempawl.mlkit_playground.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rempawl.image.processing.ui.ImageProcessingScreen
import com.rempawl.mlkit_playground.ui.theme.MlKitplaygroundTheme

// todo compose ui tests
// todo compare performance between compose and viewbinding
class MainActivity : ComponentActivity() {

    // todo show bottomsheet with camera or gallery picker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MlKitplaygroundTheme {
                ImageProcessingScreen()
            }
        }
    }

}