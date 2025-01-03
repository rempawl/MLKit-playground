plugins {
    id("com.rempawl.shared.kotlin")
    kotlin("plugin.serialization") version "2.1.0"
}
dependencies {
    implementation(libs.kotlinx.serialization.json)
}