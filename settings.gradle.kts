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
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        // Aggiungi il flatDir qui:
        flatDir {
            dirs("unityLibrary/libs")
        }
    }
}

rootProject.name = "NativeSparksApp"
include(":app", ":unityLibrary")
project(":unityLibrary").projectDir = File(rootDir, "unityLibrary")
