package com.rempawl.mlkit_playground

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.provider.MediaStore
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.platform.app.InstrumentationRegistry
import com.rempawl.image.processing.ui.ImageProcessingScreen
import com.rempawl.mlkit_playground.di.appModule
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test
import java.io.File

class ImageProcessingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule(listOf(appModule))

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun whenInitializedThenFabAndTopBarDisplayed() {
        composeTestRule.setContent {
            ImageProcessingScreen()
        }
        composeTestRule
            .onNodeWithText("Select image to run object detection")
            .isDisplayed()

        composeTestRule
            .onNode(hasClickAction())
            .assertExists()
            .assertIsEnabled()
            .assertIsDisplayed()
            .assertHeightIsEqualTo(56.dp)
            .assertWidthIsEqualTo(56.dp)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun whenFabClickedThenImagePickerSourceBottomSheetDisplayed() {
        composeTestRule.setContent {
            ImageProcessingScreen()
        }
        composeTestRule.run {
            //FIXME when more than one clickable elements it probably won't work as expected
            onNode(hasClickAction())
                .performClick()
            val bottomSheetHeaderMatcher = hasText("Select image source")
            waitUntilAtLeastOneExists(bottomSheetHeaderMatcher)

            onNode(bottomSheetHeaderMatcher)
                .assertIsDisplayed()

            onNode(hasText("Camera").and(hasClickAction()))
                .assertIsDisplayed()

            onNode(hasText("Gallery").and(hasClickAction()))
                .assertIsDisplayed()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun whenGalleryOptionClickedAndImagePickedThenImageProcessingContentDisplayed() {
        setupIntent()
        composeTestRule.setContent {
            ImageProcessingScreen()
        }
        composeTestRule.run {
            //FIXME when more than one clickable elements it probably won't work as expected
            onNode(hasClickAction()).performClick()

            val galleryButtonMatcher = hasText("Gallery")
            waitUntilAtLeastOneExists(galleryButtonMatcher)
            onNode(galleryButtonMatcher.and(hasClickAction())).performClick()

            val objectsHeaderMatcher = hasText("Detected objects")
            waitUntilAtLeastOneExists(objectsHeaderMatcher, 3000L)

            onNode(objectsHeaderMatcher).assertIsDisplayed()

            onNode(hasText("Detected texts"))
                .performScrollTo()
                .assertIsDisplayed()
        }
        releaseIntent()
    }

    private fun setupIntent() {
        Intents.init()

        val testAssets = InstrumentationRegistry.getInstrumentation().context.assets
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(targetContext.filesDir, "test.jpg")
        val fileUri = file.toUri()
        testAssets.open("screenshot-test.png").use {
            file.writeBytes(it.readBytes())
        }
        val resultData = Intent().apply { data = fileUri }

        intending(hasAction(MediaStore.ACTION_PICK_IMAGES)).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK, resultData
            )
        )
    }

    private fun releaseIntent() {
        Intents.release()
    }

}