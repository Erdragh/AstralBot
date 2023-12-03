import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.architectury.plugin.ArchitectPluginExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

plugins {
    // This is an Architectury repository, as such the relevant plugins are needed
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.3-SNAPSHOT" apply false
    // The shadow plugin is used in both Architectury and when including JDA and Exposed
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    // Since this mod/bot is written in Kotlin and expected to run on Minecraft and as such
    // the JVM, the Kotlin plugin is needed
    kotlin("jvm") version "1.9.21"
    java
}

architectury {
    val minecraftVersion: String by project
    minecraft = minecraftVersion
}

subprojects {
    // All subprojects need Architectury and Kotlin
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "kotlin")

    // Gets some values from the gradle.properties files in the
    // sub- and root projects
    val minecraftVersion: String by project
    val modLoader = project.name
    val modId = rootProject.name
    val isCommon = modLoader == rootProject.projects.common.name

    base {
        // This will be the final name of the exported JAR file
        archivesName.set("$modId-$modLoader-$minecraftVersion")
    }

    configure<LoomGradleExtensionAPI> {
        silentMojangMappingsLicense()
    }

    repositories {
        mavenCentral()
        maven(url = "https://maven.architectury.dev/")
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

    // This array gets used at multiple places, so it's easier to
    // just specify all dependencies at once and re-use them. This
    // also makes changing them later on easier.
    val botDependencies = arrayOf(
        // Library used to communicate with Discord, see https://jda.wiki
        "net.dv8tion:JDA:$jdaVersion",

        // Library to interact with the SQLite database,
        // see: https://github.com/JetBrains/Exposed
        "org.jetbrains.exposed:exposed-core:$exposedVersion",
        "org.jetbrains.exposed:exposed-dao:$exposedVersion",
        "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
        "org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion",

        // Database driver that allows Exposed to communicate with
        // the SQLite database
        "org.xerial:sqlite-jdbc:$sqliteJDBCVersion"
    )

    dependencies {
        // Minecraft Mod dependencies
        "minecraft"("::$minecraftVersion")

        @Suppress("UnstableApiUsage")
        "mappings"(project.the<LoomGradleExtensionAPI>().layered {
            val parchmentVersion: String by project

            officialMojangMappings()

            parchment(
                create(
                    group = "org.parchmentmc.data",
                    name = "parchment-$minecraftVersion",
                    version = parchmentVersion
                )
            )
        })

        // Discord Bot dependencies
        botDependencies.forEach {
            implementation(it) {
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
    }

    tasks.jar {
        // Results in the not remapped jars having a -dev at the end
        archiveClassifier.set("dev")
    }

    tasks.named<RemapJarTask>("remapJar") {
        // Results in the remapped jar not having any extra bit in
        // its file name, identifying it as the main distribution
        archiveClassifier.set(null as String?)
    }

    tasks.processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        filesMatching(listOf("META-INF/mods.toml", "fabric.mod.json")) {
            expand("version" to project.version)
        }
    }

    if (!isCommon) {
        // The subprojects for the actual mod loaders need the common
        // project and the dependencies shadowed into the jar, so the
        // plugin is used here.
        apply(plugin = "com.github.johnrengelman.shadow")

        configure<ArchitectPluginExtension> {
            platformSetupLoomIde()
        }

        // This shadowCommon configuration is used to both shadow the
        // common project and shadow the dependencies into the final
        // JARs
        val shadowCommon by configurations.creating {
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        dependencies {
            botDependencies.forEach {
                shadowCommon(it) {
                    // opus-java is for audio, which this bot doesn't need
                    exclude(module = "opus-java")
                    // Kotlin would be included as a transitive dependency
                    // on JDA and Exposed, but is already provided by the
                    // respective Kotlin implementation of the mod loaders
                    exclude(group = "org.jetbrains.kotlin")
                    exclude(group = "org.jetbrains.kotlinx")
                    // Minecraft already ships with a logging system
                    exclude(group = "org.slf4j")
                }
            }
        }

        tasks {
            "shadowJar"(ShadowJar::class) {
                // The resulting JAR of this task will be named ...-dev-shadow,
                // as it has the dependencies shadowed into it, but hasn't been
                // remapped yet.
                archiveClassifier.set("dev-shadow")
                configurations = listOf(shadowCommon)

                // Forge restricts loading certain classes for security reasons.
                // Luckily, shadow can relocate them to a different package.
                relocate("org.apache.commons.collections4", "dev.erdragh.shadowed.org.apache.commons.collections4")

                exclude(".cache/**") //Remove datagen cache from jar.
                exclude("**/astralbot/datagen/**") //Remove data gen code from jar.
                exclude("**/org/slf4j/**")
            }

            "remapJar"(RemapJarTask::class) {
                // This results in the remapped JAR being based on the JAR
                // with the dependencies and common project shadowed into it.
                dependsOn("shadowJar")
                inputFile.set(named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
            }
        }
    } else {
        sourceSets.main.get().resources.srcDir("src/main/generated/resources")
    }
}

kotlin {
    jvmToolchain(17)
}