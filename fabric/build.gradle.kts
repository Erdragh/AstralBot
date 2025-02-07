import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions

plugins {
    id("multiloader-loader")
    alias(libs.plugins.loom)
}

val modId: String by project

val botLib: Configuration by configurations.getting

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${libs.versions.parchmentMC.get()}:${libs.versions.parchment.get()}@zip")
    })
    modImplementation(libs.fabricLoader)
    modImplementation(libs.fabricApi)

    modImplementation(libs.flk)

    modApi(libs.fcapi.fabric)

    botLib.dependencies.forEach { include(it) }
}

loom {
    val aw = project(":common").file("src/main/resources/${modId}.accesswidener")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }
    mixin {
        defaultRefmapName.set("${modId}.refmap.json")
    }
    runs {
        named("client") {
            client()
            setConfigName("Fabric Client")
            ideConfigGenerated(true)
            mkdir("runs/server")
            runDir("runs/client")
        }
        named("server") {
            server()
            setConfigName("Fabric Server")
            ideConfigGenerated(true)
            mkdir("runs/server")
            runDir("runs/server")
        }
    }
}

publishMods {
    val minecraftVersion = libs.versions.minecraft.get()
    val modName: String by project
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
        displayName = "$modName $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("fabric-language-kotlin", "forge-config-api-port-fabric", "fabric-api")
    }

    modrinth("modrinthFabric") {
        from(modrinthPublish)
        modLoaders.add(project.name)
        file.set(tasks.remapJar.get().archiveFile)
        additionalFiles.plus(tasks.sourcesJar.get().archiveFile)
        displayName = "$modName $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("fabric-language-kotlin", "forge-config-api-port", "fabric-api")
    }
}