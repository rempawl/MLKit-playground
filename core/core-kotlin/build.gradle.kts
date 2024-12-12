plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
dependencies{
    api(libs.koin.core)

    api(platform(libs.arrow.stack))
    api(libs.arrow.core)
    api(libs.arrow.fx.coroutines)

    api(libs.kotlinx.coroutines.android)
}