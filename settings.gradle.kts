enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "AstralBot"

pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForge" }
        maven("https://repo.spongepowered.org/repository/maven-public/") { name = "Sponge Snapshots" }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        register("libs")
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

include("common", "fabric", "neoforge")