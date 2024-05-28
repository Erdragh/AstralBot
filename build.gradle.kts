import org.jetbrains.kotlin.gradle.utils.extendsFrom
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

plugins {
    // The shadow plugin is used by the fabric subproject to include dependencies
    // I'm temporarily using a fork of the original plugin to resolve "Unsupported java classfile major version 65"
    // see: https://github.com/johnrengelman/shadow/issues/911
    id("io.github.goooler.shadow") version "8.1.7" apply false
    // Since this mod/bot is written in Kotlin and expected to run on Minecraft and as such
    // the JVM, the Kotlin plugin is needed
    kotlin("jvm") version "2.0.0"
    // For generating documentation based on comments in the code
    id("org.jetbrains.dokka") version "1.9.10"
    java
    // Required for NeoGradle
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    // For publishing the mod
    id("me.modmuss50.mod-publish-plugin") version "0.5.1"
}

val minecraftVersion: String by project

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    // Gets some values from the gradle.properties files in the
    // sub- and root projects
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
                exclude(group = "org.jetbrains.kotlin")
                exclude(group = "org.jetbrains.kotlinx")
            }
            shadowBotDep(it) {
                // opus-java is for audio, which this bot doesn't need
                exclude(module = "opus-java")
                // Kotlin would be included as a transitive dependency
                // on JDA and Exposed, but is already provided by the
                // respective Kotlin implementation of the mod loaders
                exclude(group = "org.jetbrains.kotlin")
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

    if (!isCommon) {
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

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        targetCompatibility = JavaVersion.VERSION_17.majorVersion
        sourceCompatibility = JavaVersion.VERSION_17.majorVersion
    }

    // Publishing settings
    publishMods {
        // These titles get used based on subproject name
        val titles by extra {
            mapOf(
                "fabric" to "Fabric",
                "neoforge" to "NeoForge",
                "forge" to "Forge",
                "quilt" to "Quilt"
            )
        }
        val curseforgePublish by extra {
            curseforgeOptions {
                accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
                minecraftVersions.add(minecraftVersion)
                projectId = providers.environmentVariable("CURSEFORGE_ID")
                embeds("sqlite-jdbc")
            }
        }
        val modrinthPublish by extra {
            modrinthOptions {
                accessToken = providers.environmentVariable("MODRINTH_TOKEN")
                minecraftVersions.add(minecraftVersion)
                projectId = providers.environmentVariable("MODRINTH_ID")
                embeds("sqlite-jdbc")
            }
        }
        val changelog by extra {
            // Only gets the lines for the latest version from the Changelog
            // file. This allows me to keep all previous changes in the file
            // without having to worry about them being included on new file
            // uploads.
            File("CHANGELOG.md")
                .readText(StandardCharsets.UTF_8)
                .replace(Regex("[^^](#(#|\\n|.)+)|(^#.+)"), "")
                .trim()
        }
        val type by extra {
            STABLE
        }
    }
}

kotlin {
    jvmToolchain(17)
}


// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}