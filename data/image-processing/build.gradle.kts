plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.rempawl.shared.android")
    id("kotlin-parcelize")

}

android {
    namespace = "com.rempawl.data.image.processing"
}

dependencies {
    implementation(project(":core:core-kotlin"))
    implementation(project(":core:core-android"))

    implementation(libs.objects.detection.custom)
    implementation(libs.play.services.mlkit.text.recognition)

    implementation(libs.koin.android)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

}