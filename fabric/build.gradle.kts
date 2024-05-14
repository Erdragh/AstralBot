import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("io.github.goooler.shadow")
}

val modId: String by project

val includeBotDep: Configuration by configurations.getting
val shadowBotDep: Configuration by configurations.getting

dependencies {
    mappings(loom.officialMojangMappings())
    val minecraftVersion: String by project
    val fabricLoaderVersion: String by project
    val fabricApiVersion: String by project
    val fabricKotlinVersion: String by project
    val forgeConfigAPIVersion: String by project

    minecraft("com.mojang:minecraft:${minecraftVersion}")

    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = fabricLoaderVersion)
    modApi(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "$fabricApiVersion+$minecraftVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")

    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:$forgeConfigAPIVersion")

    includeBotDep.dependencies.forEach { include(it) }
}

loom {
    if (project(":common").file("src/main/resources/${modId}.accesswidener").exists())
        accessWidenerPath.set(project(":common").file("src/main/resources/${modId}.accesswidener"))

    @Suppress("UnstableApiUsage")
    mixin { defaultRefmapName.set("${modId}.refmap.json") }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

tasks {
    withType<JavaCompile> {
        source(project(":common").sourceSets.main.get().allSource)
    }
    withType<KotlinCompile> {
        source(project(":common").sourceSets.main.get().allSource)
    }

    javadoc { source(project(":common").sourceSets.main.get().allJava) }

    jar {
        archiveClassifier.set("dev")
    }

    shadowJar {
        archiveClassifier.set("dev-shadow")

        configurations = listOf(shadowBotDep)

        // This transforms the service files to make relocated Exposed work (see: https://github.com/JetBrains/Exposed/issues/1353)
        mergeServiceFiles()

        // Relocating Exposed somewhere different so other mods not doing that don't run into issues (e.g. Ledger)
        relocate("org.jetbrains.exposed", "dev.erdragh.shadowed.org.jetbrains.exposed")

        // Relocating jackson to prevent incompatibilities with other mods also bundling it (e.g. GroovyModLoader on Forge)
        relocate("com.fasterxml.jackson", "dev.erdragh.shadowed.com.fasterxml.jackson")

        exclude(".cache/**") //Remove datagen cache from jar.
        exclude("**/astralbot/datagen/**") //Remove data gen code from jar.
        exclude("**/org/slf4j/**")

        exclude("kotlinx/**")
        exclude("_COROUTINE/**")
        exclude("**/org/jetbrains/annotations/*")
        exclude("**/org/intellij/**")
    }

    remapJar {
        inputFile.set(named<ShadowJar>("shadowJar").get().archiveFile)
        dependsOn("shadowJar")
    }

    named("sourcesJar", Jar::class) { from(project(":common").sourceSets.main.get().allSource) }

    processResources { from(project(":common").sourceSets.main.get().resources) }
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