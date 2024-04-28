plugins {
    idea
    `maven-publish`
    id("net.minecraftforge.gradle") version "6.0.22"
    id("org.spongepowered.mixin") version "0.7-SNAPSHOT"
}

val modId: String by project
val minecraftVersion: String by project

mixin {
    add(sourceSets.main.get(), "$modId.refmap.json")
    config("$modId-common.mixins.json")
    config("$modId.mixins.json")
}

minecraft {
    mappings("official", minecraftVersion)

    copyIdeResources = true //Calls processResources when in dev

    // Automatically enable forge AccessTransformers if the file exists
    // This location is hardcoded in Forge and can not be changed.
    // https://github.com/MinecraftForge/MinecraftForge/blob/be1698bb1554f9c8fa2f58e32b9ab70bc4385e60/fmlloader/src/main/java/net/minecraftforge/fml/loading/moddiscovery/ModFile.java#L123
    val transformerFile = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (transformerFile.exists())
        accessTransformer(transformerFile)

    runs {
        create("client") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            taskName("Client")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create("modRun") {
                    source(sourceSets.main.get())
                    source(project(":common").sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            taskName("Server")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create("modServerRun") {
                    source(sourceSets.main.get())
                    source(project(":common").sourceSets.main.get())
                }
            }
        }

        create("data") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            args(
                "--mod", modId,
                "--all",
                "--output", file("src/generated/resources").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
            taskName("Data")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create("modDataRun") {
                    source(sourceSets.main.get())
                    source(project(":common").sourceSets.main.get())
                }
            }
        }
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

dependencies {
    compileOnly(project(":common"))
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT:processor")

    val minecraftVersion: String by project
    val forgeVersion: String by project
    val kotlinForgeVersion: String by project
    val forgeConfigAPIVersion: String by project

    minecraft(group = "net.minecraftforge", name = "forge", version = "$minecraftVersion-$forgeVersion")
    // Adds KFF as dependency and Kotlin libs
    implementation("thedarkcolour:kotlinforforge:$kotlinForgeVersion")

    // On 1.20.2 upwards, the forge config api port either reimplements neo's
    // or lexforge's config API. I chose to use Neo's by default, resulting in
    // an additional dependency on the lexforge side.
    implementation("fuzs.forgeconfigapiport:forgeconfigapiport-forge:$forgeConfigAPIVersion")
}

tasks {
    withType<JavaCompile> { source(project(":common").sourceSets.main.get().allSource) }

    javadoc { source(project(":common").sourceSets.main.get().allJava) }

    named("sourcesJar", Jar::class) { from(project(":common").sourceSets.main.get().allSource) }

    processResources { from(project(":common").sourceSets.main.get().resources) }

    jar { finalizedBy("reobfJar") }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get()
            artifact(tasks.jar)
            fg.component(this)
        }
    }

    repositories {
        maven("file://${System.getenv("local_maven")}")
    }
}

sourceSets.forEach {
    val dir = layout.buildDirectory.dir("sourceSets/${it.name}")
    it.output.setResourcesDir(dir)
    it.java.destinationDirectory.set(dir)
}