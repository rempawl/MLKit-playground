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
    implementation(project(":core:core-kotlin"))

    api(libs.kotlinx.coroutines.test)
    api(libs.mockk)
    api(libs.junit.jupiter)
    api(libs.turbine)
    api(libs.kotlin.test.junit)
    api(libs.koin.test.junit4)
}