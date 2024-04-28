enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "astralbot"

pluginManagement {
    repositories {
        mavenCentral()
        maven(url = "https://maven.architectury.dev/")
        maven(url = "https://maven.neoforged.net/releases/")
        maven(url = "https://maven.resourcefulbees.com/repository/maven-public/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("common")
include("fabric")
include("forge")
include("neoforge")