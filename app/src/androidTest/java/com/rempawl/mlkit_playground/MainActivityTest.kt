package com.rempawl.mlkit_playground

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rempawl.mlkit_playground.ui.MainActivity
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

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

    /*  todo compose tests  @Test
        fun whenInitializedThenClickOnFab() {
            Espresso.onView(withId(R.id.fab_select_image)).check(matches(isDisplayed()))
                .check(matches(isClickable()))

            Espresso.onView(withId(R.id.fab_select_image)).perform(click())
        }

        @Test
        fun whenImagePickedAndProcessedThenResultsAreVisible() {
            setupIntent()

            Espresso.onView(withId(R.id.fab_select_image)).check(matches(isDisplayed()))
                .check(matches(isClickable()))

            Espresso.onView(withId(R.id.fab_select_image)).perform(click())
                .check(matches(isDisplayed()))

            Espresso.onIdle()
            Espresso.onView(withId(R.id.progress))
                .check(matches(isDisplayed()))

            Espresso.onIdle {
                Thread.sleep(1000L) // alternative to adding idlingResources in viewmodel
            }

            Espresso.onView(withId(R.id.title_detected_objects))
                .check { view, noViewFoundException ->
                    Thread.sleep(1000L)
                }

            Espresso.onView(withId(R.id.title_detected_objects)).check(matches(isDisplayed()))
            Espresso.onView(withId(R.id.image_objects)).check(matches(isDisplayed()))

            Espresso.onView(withId(R.id.image_text)).perform(scrollCompletelyTo())
                .check(matches(isDisplayed()))
            Espresso.onView(withId(R.id.title_detected_texts))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
            releaseIntent()
        }*/
}