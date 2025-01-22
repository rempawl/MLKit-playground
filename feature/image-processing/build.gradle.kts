plugins {
    id("com.rempawl.shared.feature")
}

android {
    namespace = "com.rempawl.image.processing"
}

dependencies {
    implementation(project(":domain:image-processing"))

    implementation(libs.coil.compose)
    implementation(libs.coil)
}