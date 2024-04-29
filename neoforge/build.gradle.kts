import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `maven-publish`
    id("net.neoforged.gradle.userdev") version "7.0.109"
}

val modId: String by project

jarJar.enable()

// Automatically enable neoforge AccessTransformers if the file exists
// This location is hardcoded in FML and can not be changed.
// https://github.com/neoforged/FancyModLoader/blob/a952595eaaddd571fbc53f43847680b00894e0c1/loader/src/main/java/net/neoforged/fml/loading/moddiscovery/ModFile.java#L118
val transformerFile = file("src/main/resources/META-INF/accesstransformer.cfg")
if (transformerFile.exists())
    minecraft.accessTransformers.file(transformerFile)

runs {
    configureEach { modSource(project.sourceSets.main.get()) }

    create("client") { systemProperty("neoforge.enabledGameTestNamespaces", modId) }

    create("server") {
        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        programArgument("--nogui")
    }

    create("gameTestServer") { systemProperty("neoforge.enabledGameTestNamespaces", modId) }

    create("data") {
        programArguments.addAll(
            "--mod", modId,
            "--all",
            "--output", file("src/generated/resources").absolutePath,
            "--existing", file("src/main/resources/").absolutePath
        )
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

dependencies {
    val minecraftVersion: String by project
    val neoVersion: String by project
    val kotlinForgeVersion: String by project

    implementation(group = "net.neoforged", name = "neoforge", version = neoVersion)
    // Adds KFF as dependency and Kotlin libs
    implementation("thedarkcolour:kotlinforforge:$kotlinForgeVersion")

    val botDep by configurations.getting
    botDep.dependencies.forEach {
        jarJar(it)
    }
}

// NeoGradle compiles the game, but we don't want to add our common code to the game's code
val notNeoTask: Spec<Task> = Spec { !it.name.startsWith("neo") }

tasks {
    withType<JavaCompile>().matching(notNeoTask).configureEach { source(project(":common").sourceSets.main.get().allSource) }
    withType<KotlinCompile>().matching(notNeoTask).configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }

    withType<Javadoc>().matching(notNeoTask).configureEach { source(project(":common").sourceSets.main.get().allJava) }

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