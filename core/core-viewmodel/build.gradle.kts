plugins {
    id("com.rempawl.shared.android")
}

android {
    namespace = "com.rempawl.core.viewmodel"
}
dependencies {
    implementation(project(":core:core-kotlin"))
    implementation(project(":core:core-android"))
}