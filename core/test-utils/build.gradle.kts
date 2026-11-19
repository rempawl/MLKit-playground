plugins {
    id("com.rempawl.shared.android")
}

android {
    namespace = "com.rempawl.core.test.utils"
}

dependencies {
    implementation(project(":core:core-kotlin"))
    implementation(project(":core:core-viewmodel"))

    api(libs.kotlinx.coroutines.test)
    api(libs.mockk)
    api(libs.junit.jupiter)
    api(libs.turbine)
    api(libs.kotlin.test.junit)
    api(libs.koin.test.junit4)
}