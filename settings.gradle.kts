enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "AstralBot"

pluginManagement {
    repositories {
        mavenCentral()
        maven(url = "https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://repo.spongepowered.org/repository/maven-public/") { name = "Sponge Snapshots" }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("common", "fabric", "forge")