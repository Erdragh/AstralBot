import org.spongepowered.gradle.vanilla.repository.MinecraftPlatform

plugins {
    idea
    java
    `maven-publish`
    id ("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

val minecraftVersion: String by project
val modId: String by project

minecraft {
    version(minecraftVersion)
    platform(MinecraftPlatform.SERVER)
    if (file("src/main/resources/${modId}.accesswidener").exists())
        accessWideners(file("src/main/resources/${modId}.accesswidener"))
}

dependencies {
    val fabricLoaderVersion: String by project
    val forgeConfigAPIVersion: String by project

    api("fuzs.forgeconfigapiport:forgeconfigapiport-common-neoforgeapi:$forgeConfigAPIVersion")
    compileOnly("org.spongepowered:mixin:0.8.5")
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
        maven("file://${System.getenv("local_maven")}")
    }
}