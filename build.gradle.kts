import org.jetbrains.kotlin.gradle.utils.extendsFrom
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

plugins {
    // The shadow plugin is used by the fabric subproject to include dependencies
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    // Since this mod/bot is written in Kotlin and expected to run on Minecraft and as such
    // the JVM, the Kotlin plugin is needed
    kotlin("jvm") version "1.9.23"
    // For generating documentation based on comments in the code
    id("org.jetbrains.dokka") version "1.9.10"
    java
    // Required for NeoGradle
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.dokka")

    // Gets some values from the gradle.properties files in the
    // sub- and root projects
    val minecraftVersion: String by project
    val modLoader = project.name
    val modId: String by project
    val modName = rootProject.name
    val modAuthor: String by project
    val isCommon = modLoader == rootProject.projects.common.name

    base {
        // This will be the final name of the exported JAR file
        archivesName.set("$modId-$modLoader-$minecraftVersion")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        maven(url = "https://maven.neoforged.net/releases/")
        maven("https://repo.spongepowered.org/repository/maven-public/") { name = "Sponge / Mixin" }
        maven("https://maven.blamejared.com") { name = "BlameJared Maven (JEI / CraftTweaker / Bookshelf)" }
        // For the parchment mappings
        maven(url = "https://maven.parchmentmc.org")
        maven(url = "https://maven.resourcefulbees.com/repository/maven-public/")
        maven {
            name = "Kotlin for Forge"
            setUrl("https://thedarkcolour.github.io/KotlinForForge/")
        }
        // Forge Config API port
        maven {
            name = "Fuzs Mod Resources"
            setUrl("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
        }
    }

    // Bot dependencies
    val jdaVersion: String by project
    val exposedVersion: String by project
    val sqliteJDBCVersion: String by project
    val commonmarkVersion: String by project

    // Configuration for shaded dependencies, get relocated to dev.erdragh.astralbot.shadowed
    val shadowBotDep by configurations.creating {
        isTransitive = true
    }
    // Configuration for JiJ-ed dependencies
    val includeBotDep by configurations.creating {
        isTransitive = false
    }
    // Configuration for libraries that are needed at runtime
    val runtimeLib by configurations.creating {
        isTransitive = true
    }
    configurations.implementation.extendsFrom(configurations.named("shadowBotDep"))
    configurations.implementation.extendsFrom(configurations.named("includeBotDep"))
    configurations.implementation.extendsFrom(configurations.named("runtimeLib"))

    dependencies {
        runtimeLib("org.xerial:sqlite-jdbc:$sqliteJDBCVersion")
        includeBotDep("org.xerial:sqlite-jdbc:$sqliteJDBCVersion")

        runtimeLib("org.commonmark:commonmark:$commonmarkVersion")
        includeBotDep("org.commonmark:commonmark:$commonmarkVersion")


        arrayOf(
            // Library used to communicate with Discord, see https://jda.wiki
            "net.dv8tion:JDA:$jdaVersion",

            // Library to interact with the SQLite database,
            // see: https://github.com/JetBrains/Exposed
            "org.jetbrains.exposed:exposed-core:$exposedVersion",
            "org.jetbrains.exposed:exposed-dao:$exposedVersion",
            "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
        ).forEach {
            runtimeLib(it) {
                exclude(module = "opus-java")
                exclude(group = "org.slf4j")
            }
            shadowBotDep(it) {
                // opus-java is for audio, which this bot doesn't need
                exclude(module = "opus-java")
                // Kotlin would be included as a transitive dependency
                // on JDA and Exposed, but is already provided by the
                // respective Kotlin implementation of the mod loaders
                exclude(group = "org.jetbrains.kotlin")
                exclude(group = "kotlinx")
                // Minecraft already ships with a logging system
                exclude(group = "org.slf4j")
            }
        }
    }

    java {
        withSourcesJar()
        modularity.inferModulePath = true
    }

    tasks.jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_$modId" }
        }

        manifest {
            attributes(
                "Specification-Title" to modId,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to archiveVersion,
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to modAuthor,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                "Built-On-Minecraft" to minecraftVersion
            )
        }
    }

    tasks.processResources {
        val version: String by project
        val group: String by project
        val minecraftVersionRange: String by project
        val fabricApiVersion: String by project
        val fabricLoaderVersion: String by project
        val fabricKotlinVersion: String by project
        val neoVersion: String by project
        val neoVersionRange: String by project
        val kffLoaderRange: String by project
        val license: String by project
        val description: String by project
        val credits: String by project

        val expandProps = mapOf(
            "version" to version,
            "group" to group, //Else we target the task's group.
            "minecraft_version" to minecraftVersion,
            "minecraft_version_range" to minecraftVersionRange,
            "fabric_version" to fabricApiVersion,
            "fabric_loader_version" to fabricLoaderVersion,
            "fabric_kotlin_version" to fabricKotlinVersion,
            "neoforge_version" to neoVersion,
            "neoforge_loader_version_range" to neoVersionRange,
            "kff_loader_range" to kffLoaderRange,
            "mod_name" to modName,
            "mod_author" to modAuthor,
            "mod_id" to modId,
            "license" to license,
            "description" to description,
            "credits" to credits
        )

        filesMatching(listOf("pack.mcmeta", "*.mixins.json", "META-INF/mods.toml", "fabric.mod.json")) {
            expand(expandProps)
        }
        inputs.properties(expandProps)
    }

    if (isCommon) {
        sourceSets.main.get().resources.srcDir("src/main/generated/resources")
    } else {
        dependencies {
            implementation(project(":common"))
        }
    }

    // Disables Gradle's custom module metadata from being published to maven. The
    // metadata includes mapped dependencies which are not reasonably consumable by
    // other mod developers.
    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.register<Task>("prepareChangelog") {
    group = "common"
    description = "Prepares the changelog by removing irrelevant parts from Changelog.md"
    var changelog = File("Changelog.md").readText(StandardCharsets.UTF_8)
    changelog = changelog.replace(Regex("[^^](#(#|\\n|.)+)|(^#.+)"), "")
    println(changelog.trim())
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}