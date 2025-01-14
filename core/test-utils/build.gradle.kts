plugins {
    id("com.rempawl.shared.android")
}

android {
    namespace = "com.rempawl.core.test.utils"
}

dependencies {
    implementation(project(":core:core-kotlin"))
    implementation(project(":core:core-viewmodel"))

    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.mockk)
    implementation(libs.junit.jupiter)
    implementation(libs.turbine)
    implementation(libs.kotlin.test.junit)
    implementation(libs.koin.test.junit4)
}