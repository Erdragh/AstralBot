import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("multiloader-loader")
    alias(libs.plugins.moddev)
}

val modId: String by project

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}.mixins.json")
    config("${modId}.forge.mixins.json")
}
tasks.jar {
    manifest {
        attributes["MixinConfigs"] = "${modId}.mixins.json,${modId}.forge.mixins.json"
    }
}

legacyForge {
    version = libs.versions.forge.get()
    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = libs.versions.parchmentMC.get()
        mappingsVersion = libs.versions.parchment.get()
    }
    runs {
        configureEach {
            systemProperty("forge.enabledGameTestNamespaces", modId)
            ideName = "Forge ${name.capitalized()} (${project.path})" // Unify the run config names with fabric
        }
        register("client") {
            client()
        }
        register("data") {
            data()
        }
        register("server") {
            server()
        }
    }
    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main.get().resources { srcDir("src/generated/resources") }

dependencies {
    implementation(libs.kff)
    annotationProcessor(variantOf(libs.mixin) { classifier("processor") })

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

    curseforge("curseForge") {
        from(curseforgePublish)
        modLoaders.add(project.name)
        file.set(tasks.named<Jar>("reobfJar").get().archiveFile)
        additionalFiles.plus(tasks.sourcesJar.get().archiveFile)
        displayName = "$modName $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("kotlin-for-forge")
    }

    modrinth("modrinthForge") {
        from(modrinthPublish)
        modLoaders.add(project.name)
        file.set(tasks.named<Jar>("reobfJar").get().archiveFile)
        additionalFiles.plus(tasks.sourcesJar.get().archiveFile)
        displayName = "$modName $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("kotlin-for-forge")
    }
}