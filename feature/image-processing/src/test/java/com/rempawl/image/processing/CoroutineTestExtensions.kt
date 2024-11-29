package com.rempawl.image.processing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor

// todo core-test module
open class BaseCoroutineTest(
    override var testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : CoroutineTest

@ExtendWith(TestCoroutineExtension::class)
interface CoroutineTest {
    var testDispatcher: TestDispatcher
}

/**
 * JUnit 5 Extension for automatically creating a [StandardTestDispatcher],
 * then a [TestScope] with the same CoroutineContext.
 */
class TestCoroutineExtension : TestInstancePostProcessor, AfterAllCallback {

    override fun postProcessTestInstance(testInstance: Any?, context: ExtensionContext?) {
        (testInstance as? CoroutineTest)?.let { coroutineTest ->
            Dispatchers.setMain(coroutineTest.testDispatcher)
        }
    }

    override fun afterAll(context: ExtensionContext?) {
        Dispatchers.resetMain()
    }
}