import dev.erdragh.changelog.ChangelogExtension
import java.net.URI

plugins {
    alias(libs.plugins.cloche)
    alias(libs.plugins.kotlin)
    id("dev.erdragh.changelog-extractor")
    alias(libs.plugins.publish)
    // so we can enable soruce download
    idea
}

val changelogExtension = extensions.getByType(ChangelogExtension::class.java)

afterEvaluate {
    version = changelogExtension.changelogVersion.get()
}
group = "dev.erdragh"

repositories {
    cloche {
        librariesMinecraft()
        main()
        mavenNeoforgedMeta()
        mavenNeoforged()
        mavenFabric()
    }

    maven {
        name = "Fuzs Mod Resources"
        url = URI("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
    }
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
    mavenCentral()
}

cloche {
    metadata {
        modId = "astralbot"
        name = rootProject.name
        description = "Discord Bot and Minecraft Mod in one bundle."
        license = "MIT"
        author("Erdragh", "mail@erdragh.dev")
    }

    val externalLibs = listOf(
        libs.commonmark,
        libs.sqlite,
        // JDA
        jda.jda,
        jda.okhttp,
        jda.okio,
        jda.tink,
        jda.trove4j,
        jda.jackson.core,
        jda.jackson.databind,
        jda.jackson.annotations,
        // Exposed
        exposed.core,
        exposed.dao,
        exposed.jdbc,
        // Webhook
        dcwebhooks.json,
        dcwebhooks.webhooks
    )

    common {
        mappings {
            official()
        }

        dependencies {
            externalLibs.forEach {
                implementation(it) {
                    exclude(group = "org.slf4j")
                }
            }
        }
    }

    val fabricCommon = common("common:fabric") {
        dependencies {
            modImplementation("net.fabricmc:fabric-language-kotlin:1.13.4+kotlin.${libs.versions.kotlin.get()}")
        }
    }

    val common1211 = common("1.21.1:common") {
        mixins.from("astralbot-common.mixins.json")
    }

    val fabric1211 = fabric("1.21.1:fabric") {
        minecraftVersion = "1.21.1"
        loaderVersion = libs.versions.fabricLoader

        dependsOn(fabricCommon)
        dependsOn(common1211)

        dependencies {
            modApi("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:21.1.6")
            fabricApi("0.116.7")

            externalLibs.forEach {
                include(it)
            }
        }

        runs {
            server {
                runDir("run/1.21.1/fabric")
            }
        }
    }

    val neoforge1211 = neoforge("1.21.1:neoforge") {
        minecraftVersion = "1.21.1"
        loaderVersion = "21.1.218"

        dependsOn(common1211)

        mixins.from("astralbot-neoforge.mixins.json")

        dependencies {
            implementation("thedarkcolour:kfflang-neoforge:5.11.0")
            implementation("thedarkcolour:kffmod-neoforge:5.11.0")
            implementation("thedarkcolour:kfflib-neoforge:5.11.0")

            legacyClasspath("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
            legacyClasspath("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
            legacyClasspath(libs.kotlinx.coroutines)

            externalLibs.forEach {
                legacyClasspath(it) {
                    exclude(group = "org.jetbrains.kotlin")
                    exclude(group = "org.jetbrains.kotlinx")
                }
                include(it)
            }
        }

        runs {
            server {
                runDir("run/1.21.1/neo")
            }
        }
    }

    listOf(fabric1211).forEach {
        it.metadata {
            entrypoint("main") {
                value = "dev.erdragh.astralbot.fabric.BotMod"
                adapter = "kotlin"
            }
        }
    }
    listOf(neoforge1211).forEach {
        it.metadata {
            modLoader = "kotlinforforge"
        }
    }
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}