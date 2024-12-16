plugins {
    id("com.rempawl.shared.kotlin")
}

dependencies {
    implementation(project(":core:core-kotlin"))

    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.mockk)
    implementation(libs.junit.jupiter)
    implementation(libs.turbine)
    implementation(libs.kotlin.test.junit)
    implementation(libs.koin.test.junit4)
}