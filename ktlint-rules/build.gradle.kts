plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    compileOnly("com.pinterest.ktlint:ktlint-rule-engine-core:1.3.1")
    compileOnly("com.pinterest.ktlint:ktlint-cli-ruleset-core:1.3.1")

    testImplementation("com.pinterest.ktlint:ktlint-rule-engine-core:1.3.1")
    testImplementation("com.pinterest.ktlint:ktlint-cli-ruleset-core:1.3.1")
    testImplementation("com.pinterest.ktlint:ktlint-test:1.3.1")
    testImplementation("org.slf4j:slf4j-nop:2.0.13")
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
