import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions

plugins {
    java
    alias(libs.plugins.kotlin)
    idea
    alias(libs.plugins.loom)
    alias(libs.plugins.publish)
}

val modId: String by project

val includeBotDep: Configuration by configurations.getting

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
    serverOnlyMinecraftJar()

    if (project(":common").file("src/main/resources/${modId}.accesswidener").exists())
        accessWidenerPath.set(project(":common").file("src/main/resources/${modId}.accesswidener"))

    @Suppress("UnstableApiUsage")
    mixin {
        // TODO: Somehow figure out a way to get loom to create a refmap for common mixins
        // add(project(":common").sourceSets.main.get())
        defaultRefmapName.set("${modId}.refmap.json")
    }

    mods {
        create("astralbot") {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("run")
        }
    }
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

    curseforge("curseFabric") {
        from(curseforgePublish)
        modLoaders.add(project.name)
        file.set(tasks.remapJar.get().archiveFile)
        additionalFiles.plus(tasks.sourcesJar.get().archiveFile)
        displayName = "$title $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("fabric-language-kotlin", "forge-config-api-port-fabric", "fabric-api")
    }

    modrinth("modrinthFabric") {
        from(modrinthPublish)
        modLoaders.add(project.name)
        file.set(tasks.remapJar.get().archiveFile)
        additionalFiles.plus(tasks.sourcesJar.get().archiveFile)
        displayName = "$title $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("fabric-language-kotlin", "forge-config-api-port", "fabric-api")
    }
}