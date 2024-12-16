plugins {
    id("com.rempawl.shared.feature")
}

android {
    namespace = "com.rempawl.image.processing"
}

dependencies {
    implementation(project(":data:image-processing"))

    implementation(libs.objects.detection.custom)
    implementation(libs.play.services.mlkit.text.recognition)

    implementation(libs.coil.compose)
    implementation(libs.coil)
}