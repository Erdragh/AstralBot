import dev.erdragh.changelog.ChangelogExtension
import java.net.URI

plugins {
    alias(libs.plugins.cloche)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
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
        jda.websocket,
        jda.jackson.core,
        jda.jackson.databind,
        jda.jackson.annotations,
        jda.commons.collections,
        // Exposed
        exposed.core,
        exposed.dao,
        exposed.jdbc,
        // Webhook
        dcwebhooks.json,
        dcwebhooks.webhooks
    )

    common {
        dependencies {
            externalLibs.forEach {
                implementation(it) {
                    exclude(group = "org.slf4j")
                }
            }
        }
    }

    val remappedCommon = common("common:remapped") {
        mappings {
            official()
        }
    }

    val fabricCommon = common("common:fabric") {
        dependencies {
            modImplementation("net.fabricmc:fabric-language-kotlin:1.13.14+kotlin.${libs.versions.kotlin.get()}")
        }
        dependsOn(remappedCommon)
    }


    val common1182 = common("1.18.2:common") {
        mixins.from("src/1.18.2/common/astralbot-common.mixins.json")
        dependsOn(remappedCommon)
    }
    val common1211 = common("1.21.1:common") {
        mixins.from("src/1.21.1/common/astralbot-common.mixins.json")
        dependsOn(remappedCommon)
    }
    val common261 = common("26.1:common") {
        // TODO: mixins.from("astralbot-common.mixins.json")
        dependsOn(remappedCommon)
    }

    val fabric1182 = fabric("1.18.2:fabric") {
        minecraftVersion = "1.18.2"
        loaderVersion = libs.versions.fabricLoader

        mixins.from(file("src/1.18.2/fabric/astralbot-fabric.mixins.json"))

        dependsOn(fabricCommon)
        dependsOn(common1182)

        dependencies {
            modApi("net.minecraftforge:forgeconfigapiport-fabric:3.2.4")
            fabricApi("0.77.0")

            externalLibs.forEach {
                include(it)
            }
        }

        runs {
            server {
                runDir("run/1.18.2/fabric")
            }
        }
    }

    val fabric1211 = fabric("1.21.1:fabric") {
        minecraftVersion = "1.21.1"
        loaderVersion = libs.versions.fabricLoader

        dependsOn(fabricCommon)
        dependsOn(common1211)

        dependencies {
            modApi("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:21.1.6")
            fabricApi("0.116.18")

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

    val fabric261 = fabric("26.1:fabric") {
        minecraftVersion = "26.1.2"
        loaderVersion = libs.versions.fabricLoader

        dependsOn(fabricCommon)
        dependsOn(common261)

        dependencies {
            modApi("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:26.1.3")
            fabricApi("0.155.3")

            externalLibs.forEach {
                include(it)
            }
        }

        runs {
            server {
                runDir("run/26.1/fabric")
            }
        }
    }

    val neoforge1211 = neoforge("1.21.1:neoforge") {
        minecraftVersion = "1.21.1"
        loaderVersion = "21.1.267"

        dependsOn(common1211)

        mixins.from("src/1.21.1/neoforge/astralbot-neoforge.mixins.json")

        dependencies {
            implementation("thedarkcolour:kfflang-neoforge:5.11.0")
            implementation("thedarkcolour:kffmod-neoforge:5.11.0")
            implementation("thedarkcolour:kfflib-neoforge:5.11.0")

            legacyClasspath("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
            legacyClasspath("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
            legacyClasspath(libs.kotlinx.coroutines)

            // forge api so I can use it from 1.18.2 too
            modApi("fuzs.forgeconfigapiport:forgeconfigapiport-neoforge:21.1.6")

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

    val neoforge261 = neoforge("26.1:neoforge") {
        minecraftVersion = "26.1.2"
        loaderVersion = "26.1.2.128"

        mixins.from("astralbot-neoforge.mixins.json")

        dependsOn(common261)

        dependencies {
            implementation("thedarkcolour:kfflang-neoforge:5.11.0")
            implementation("thedarkcolour:kffmod-neoforge:5.11.0")
            implementation("thedarkcolour:kfflib-neoforge:5.11.0")

            legacyClasspath("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
            legacyClasspath("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
            legacyClasspath(libs.kotlinx.coroutines)

            modApi("fuzs.forgeconfigapiport:forgeconfigapiport-neoforge:26.1.3")

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
                runDir("run/26.1/neo")
            }
        }
    }

    listOf(fabric1182, fabric1211, fabric261).forEach {
        it.metadata {
            entrypoint("main") {
                value = "dev.erdragh.astralbot.fabric.BotMod"
                adapter = "kotlin"
            }
        }
    }
    listOf(neoforge1211, neoforge261).forEach {
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
dependencies {
    testImplementation(kotlin("test"))
}