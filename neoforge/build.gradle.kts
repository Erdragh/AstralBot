import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("multiloader-loader")
    alias(libs.plugins.moddev)
}

val modId: String by project
val botLib: Configuration by configurations.getting

neoForge {
    version = libs.versions.neoforge.get()

    parchment {
        minecraftVersion = libs.versions.parchmentMC.get()
        mappingsVersion = libs.versions.parchment.get()
    }

    validateAccessTransformers = true

    // Automatically enable neoforge AccessTransformers if the file exists
    // This location is hardcoded in FML and can not be changed.
    // https://github.com/neoforged/FancyModLoader/blob/a952595eaaddd571fbc53f43847680b00894e0c1/loader/src/main/java/net/neoforged/fml/loading/moddiscovery/ModFile.java#L118
    val transformerFile = project.file("src/main/resources/META-INF/accesstransformer.cfg")
    if (transformerFile.exists())
        accessTransformers.from(transformerFile)

    mods {
        create(modId) {
            sourceSet(project.sourceSets.main.get())
        }
    }

    runs {
        create("server") {
            server()
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            programArgument("--nogui")
        }

        create("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

dependencies {
    // Adds KFF as dependency and Kotlin libs
    implementation(libs.kff)

    configurations.named("additionalRuntimeClasspath").extendsFrom(configurations.botLib)
    configurations.jarJar.extendsFrom(configurations.botLib)
}

publishMods {
    val minecraftVersion: String = libs.versions.minecraft.get()
    val modName: String by project
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
        displayName = "$modName $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("kotlin-for-forge")
    }

    modrinth("modrinthNeo") {
        from(modrinthPublish)
        modLoaders.add(project.name)
        file.set(tasks.jar.get().archiveFile)
        additionalFiles.plus(tasks.sourcesJar.get().archiveFile)
        displayName = "$modName $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("kotlin-for-forge")
    }
}