import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `maven-publish`
    java
    id("net.neoforged.gradle.userdev") version "7.0.109"
    id("com.github.johnrengelman.shadow")
}

val modId: String by project

val botDep: Configuration by configurations.getting
val runtimeLib: Configuration by configurations.getting

// Automatically enable neoforge AccessTransformers if the file exists
// This location is hardcoded in FML and can not be changed.
// https://github.com/neoforged/FancyModLoader/blob/a952595eaaddd571fbc53f43847680b00894e0c1/loader/src/main/java/net/neoforged/fml/loading/moddiscovery/ModFile.java#L118
val transformerFile = file("src/main/resources/META-INF/accesstransformer.cfg")
if (transformerFile.exists())
    minecraft.accessTransformers.file(transformerFile)

runs {
    configureEach { modSource(project.sourceSets.main.get()) }

    create("client") {
        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        dependencies {
            botDep.dependencies.configureEach { runtime(this) }
            runtimeLib.dependencies.configureEach { runtime(this) }
        }
    }

    create("server") {
        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        programArgument("--nogui")
        dependencies {
            botDep.dependencies.configureEach { runtime(this) }
            runtimeLib.dependencies.configureEach { runtime(this) }
        }
    }

    create("gameTestServer") {
        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        dependencies {
            botDep.dependencies.configureEach { runtime(this) }
            runtimeLib.dependencies.configureEach { runtime(this) }
        }
    }

    create("data") {
        programArguments.addAll(
            "--mod", modId,
            "--all",
            "--output", file("src/generated/resources").absolutePath,
            "--existing", file("src/main/resources/").absolutePath
        )
        dependencies {
            botDep.dependencies.configureEach { runtime(this) }
            runtimeLib.dependencies.configureEach { runtime(this) }
        }
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

dependencies {
    val minecraftVersion: String by project
    val neoVersion: String by project
    val kotlinForgeVersion: String by project

    implementation(group = "net.neoforged", name = "neoforge", version = neoVersion)
    // Adds KFF as dependency and Kotlin libs
    implementation("thedarkcolour:kotlinforforge-neoforge:$kotlinForgeVersion")
}

// NeoGradle compiles the game, but we don't want to add our common code to the game's code
val notNeoTask: Spec<Task> = Spec { !it.name.startsWith("neo") }

tasks {
    withType<JavaCompile>().matching(notNeoTask).configureEach { source(project(":common").sourceSets.main.get().allSource) }
    withType<KotlinCompile>().matching(notNeoTask).configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }

    withType<Javadoc>().matching(notNeoTask).configureEach { source(project(":common").sourceSets.main.get().allJava) }

    jar {
        archiveClassifier.set("slim")
    }

    shadowJar {
        archiveClassifier.set(null as String?)

        configurations = listOf(botDep)

        // This transforms the service files to make relocated Exposed work (see: https://github.com/JetBrains/Exposed/issues/1353)
        mergeServiceFiles()

        // Forge restricts loading certain classes for security reasons.
        // Luckily, shadow can relocate them to a different package.
        relocate("org.apache.commons.collections4", "dev.erdragh.shadowed.org.apache.commons.collections4")

        // Relocating Exposed somewhere different so other mods not doing that don't run into issues (e.g. Ledger)
        relocate("org.jetbrains.exposed", "dev.erdragh.shadowed.org.jetbrains.exposed")

        // Relocating jackson to prevent incompatibilities with other mods also bundling it (e.g. GroovyModLoader on Forge)
        relocate("com.fasterxml.jackson", "dev.erdragh.shadowed.com.fasterxml.jackson")

        exclude(".cache/**") //Remove datagen cache from jar.
        exclude("**/astralbot/datagen/**") //Remove data gen code from jar.
        exclude("**/org/slf4j/**")

        exclude("kotlinx/**")
        exclude("_COROUTINE/**")
        exclude("**/org/jetbrains/annotations/*")
        exclude("**/org/intellij/**")
    }

    build {
        dependsOn("shadowJar")
    }

    named("sourcesJar", Jar::class) { from(project(":common").sourceSets.main.get().allSource) }

    processResources { from(project(":common").sourceSets.main.get().resources) }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get()
            artifact(tasks.jar)
        }
    }

    repositories {
        maven("file://${System.getenv("local_maven")}")
    }
}