import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

architectury {
    fabric()
}

val common: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
    configurations.runtimeClasspath.get().extendsFrom(this)
    configurations["developmentFabric"].extendsFrom(this)
}

dependencies {
    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    val minecraftVersion: String by project
    val fabricLoaderVersion: String by project
    val fabricApiVersion: String by project
    val fabricKotlinVersion: String by project
    val forgeConfigAPIVersion: String by project

    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = fabricLoaderVersion)
    modApi(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "$fabricApiVersion+$minecraftVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")

    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:$forgeConfigAPIVersion")
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
        requires("fabric-language-kotlin", "forge-config-api-port-fabric", "fabric-api")
    }
}