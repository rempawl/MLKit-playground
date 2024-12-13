pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MlKit-playground"
include(":app")
include(":feature:image-processing")
include(":core:core-android")
include(":core:core-ui")
include(":core:core-kotlin")
include(":core:core-viewmodel")
include(":data:image-processing")
include(":core:test-utils")
