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

    val jdaVersion: String by project

    // This *should* theoretically fix the Forge development environment not having
    // access to certain classes, but I haven't gotten it to work just yet.
    forgeRuntimeLibrary("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.slf4j")
    }
}