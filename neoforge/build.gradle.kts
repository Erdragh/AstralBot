architectury {
    neoForge()
}

loom {
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
    configurations["developmentNeoForge"].extendsFrom(this)
}

dependencies {
    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }

    val minecraftVersion: String by project
    val neoVersion: String by project
    val kotlinForgeVersion: String by project

    neoForge(group = "net.neoforged", name = "neoforge", version = neoVersion)
    // Adds KFF as dependency and Kotlin libs
    "thedarkcolour:kotlinforforge:$kotlinForgeVersion".let {
        implementation(it)
        forgeRuntimeLibrary(it)
    }

    val jdaVersion: String by project

    // This *should* theoretically fix the Forge development environment not having
    // access to certain classes, but I haven't gotten it to work just yet.
    forgeRuntimeLibrary("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.slf4j")
    }
}