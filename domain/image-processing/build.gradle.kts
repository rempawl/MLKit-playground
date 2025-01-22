plugins {
    id("com.rempawl.shared.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.rempawl.domain.image.processing"
}

dependencies {
    implementation(project(":core:core-kotlin"))
    implementation(project(":core:core-android"))

    implementation(libs.objects.detection.custom)
    implementation(libs.play.services.mlkit.text.recognition)
}