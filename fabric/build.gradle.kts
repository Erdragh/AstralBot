plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom") version "1.6-SNAPSHOT"
}

val modId: String by project

dependencies {
    compileOnly(project(":common"))
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
}

loom {
    if (project(":common").file("src/main/resources/${modId}.accesswidener").exists())
        accessWidenerPath.set(project(":common").file("src/main/resources/${modId}.accesswidener"))

    @Suppress("UnstableApiUsage")
    mixin { defaultRefmapName.set("${modId}.refmap.json") }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

tasks {
    withType<JavaCompile> { source(project(":common").sourceSets.main.get().allSource) }

    javadoc { source(project(":common").sourceSets.main.get().allJava) }

    named("sourcesJar", Jar::class) { from(project(":common").sourceSets.main.get().allSource) }

    processResources { from(project(":common").sourceSets.main.get().resources) }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
        maven("file://${System.getenv("local_maven")}")
    }
}