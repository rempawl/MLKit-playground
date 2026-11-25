plugins {
    id("groovy-gradle-plugin")
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    compileOnly("com.android.tools.build:gradle:9.5.1")
}