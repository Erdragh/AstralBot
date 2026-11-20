enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "AstralBot"

pluginManagement {
    repositories {
        maven(url = "https://maven.msrandom.net/repository/cloche/")
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        register("libs") {
            from(files("libs.versions.toml"))
        }
        register("jda") {
            from(files("gradle/jda.versions.toml"))
        }
        register("dcwebhooks") {
            from(files("gradle/dcwebhooks.versions.toml"))
        }
        register("exposed") {
            from(files("gradle/exposed.versions.toml"))
        }
    }
}