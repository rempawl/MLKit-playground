import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // for junit5 on gradle 7+
    id("de.mannodermaus.android-junit5") version "1.9.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.1.0"
    id("kotlin-parcelize")
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"

}

android {
    namespace = "com.rempawl.image.processing"

    // todo plugin gradle for shared stuff
    compileSdk = 35
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    ktlint {
        android = true
        ignoreFailures = true
        disabledRules.addAll("no-wildcard-imports", "final-newline")
        reporters {
            reporter(ReporterType.HTML)
            reporter(ReporterType.PLAIN)
        }
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
//    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf") todo
}

dependencies {
    implementation(project(":core:core-kotlin"))
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-android"))
    implementation(project(":core:core-viewmodel"))
    implementation(project(":data:image-processing"))
    // todo gradle plugin for dependencies
    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)
    implementation(libs.compose.destinations.bottomsheet)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.lifecycle.runtime.compose.android)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)

    implementation(libs.coil.compose)
    implementation(libs.coil)

    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.runtime.tracing)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlin.test.junit)
}