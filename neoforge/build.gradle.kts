import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    idea
    java
    id("multiloader-loader")
    alias(libs.plugins.moddev)
    alias(libs.plugins.publish)
}

val modId: String by project
val includeBotDep: Configuration by configurations.getting
val runtimeLib: Configuration by configurations.getting

val minecraftVersion: String by project
val mcVersion = minecraftVersion
val parchmentMinecraft: String by project
val parchmentVersion: String by project
val neoVersion: String by project
val kotlinForgeVersion: String by project

neoForge {
    version = neoVersion

    parchment {
        minecraftVersion = parchmentMinecraft
        mappingsVersion = parchmentVersion
    }

    validateAccessTransformers = true

    // Automatically enable neoforge AccessTransformers if the file exists
    // This location is hardcoded in FML and can not be changed.
    // https://github.com/neoforged/FancyModLoader/blob/a952595eaaddd571fbc53f43847680b00894e0c1/loader/src/main/java/net/neoforged/fml/loading/moddiscovery/ModFile.java#L118
    val transformerFile = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (transformerFile.exists())
        accessTransformers.from(transformerFile)

    mods {
        create(modId) {
            sourceSet(project.sourceSets.main.get())
            sourceSet(project(":common").sourceSets.main.get())
        }
    }

    runs {
        create("server") {
            server()
            programArgument("--nogui")
        }

        create("gameTestServer") {
            type = "gameTestServer"
        }

        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            ideName = "NeoForge ${name.capitalized()} (${project.path})"
        }
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

dependencies {
    // Adds KFF as dependency and Kotlin libs
    implementation("thedarkcolour:kotlinforforge-neoforge:$kotlinForgeVersion")

    configurations.additionalRuntimeClasspath.extendsFrom(configurations.runtimeLib)
    configurations.jarJar.extendsFrom(configurations.includeBotDep)
}

publishMods {
    val minecraftVersion: String by project
    val title: String by project
    val version: String by project

    val titles: Map<String, String> by extra
    val curseforgePublish: Provider<CurseforgeOptions> by extra
    val modrinthPublish: Provider<ModrinthOptions> by extra

    changelog = extra.get("changelog") as String
    type = extra.get("type") as ReleaseType

    curseforge("curseNeo") {
        from(curseforgePublish)
        modLoaders.add(project.name)
        file.set(tasks.jar.get().archiveFile)
        additionalFiles.plus(tasks.sourcesJar.get().archiveFile)
        displayName = "$title $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("kotlin-for-forge")
    }

    modrinth("modrinthNeo") {
        from(modrinthPublish)
        modLoaders.add(project.name)
        file.set(tasks.jar.get().archiveFile)
        additionalFiles.plus(tasks.sourcesJar.get().archiveFile)
        displayName = "$title $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("kotlin-for-forge")
    }
}