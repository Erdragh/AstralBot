import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions

architectury {
    forge()
}

loom {
    forge {
        mixinConfig("astralbot-common.mixins.json")
        mixinConfig("astralbot.mixins.json")
    }
    // This sets up data generation. At the time of writing this
    // Comment, this is useless, as there are no resources to be
    // generated. I want to keep it in as a reference tho.
    runs {
        create("data") {
            data()
            programArgs("--all", "--mod", "astralbot")
            programArgs("--output", project(":common").file("src/main/generated/resources").absolutePath)
            programArgs("--existing", project(":common").file("src/main/resources").absolutePath)
        }
    }
}

val common: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
    configurations.runtimeClasspath.get().extendsFrom(this)
    configurations["developmentForge"].extendsFrom(this)
}

val runtimeLib by configurations.getting

dependencies {
    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "transformProductionForge")) {
        isTransitive = false
    }

    val minecraftVersion: String by project
    val forgeVersion: String by project
    val kotlinForgeVersion: String by project

    forge(group = "net.minecraftforge", name = "forge", version = "$minecraftVersion-$forgeVersion")
    // Adds KFF as dependency and Kotlin libs
    implementation("thedarkcolour:kotlinforforge:$kotlinForgeVersion")

    // This *should* theoretically fix the Forge development environment not having
    // access to certain classes, but I haven't gotten it to work just yet.
    runtimeLib.dependencies.forEach(::forgeRuntimeLibrary)
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

    curseforge("curseForge") {
        from(curseforgePublish)
        modLoaders.add(project.name)
        file.set(tasks.remapJar.get().archiveFile)
        displayName = "$title $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("kotlin-for-forge")
    }

    modrinth("modrinthForge") {
        from(modrinthPublish)
        modLoaders.add(project.name)
        file.set(tasks.remapJar.get().archiveFile)
        displayName = "$title $version ${titles[project.name]} $minecraftVersion"
        this.version = "$version-mc$minecraftVersion-${project.name}"
        requires("kotlin-for-forge")
    }
}