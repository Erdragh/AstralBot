plugins {
    idea
    java
    id("multiloader-common")
    alias(libs.plugins.moddev)
}

val neoformVersion: String by project
val minecraftVersion: String by project
val mcVersion = minecraftVersion
val parchmentVersion: String by project
val parchmentMinecraft: String by project
val modId: String by project

neoForge {
    neoFormVersion = neoformVersion

    // Automatically enable neoforge AccessTransformers if the file exists
    // This location is hardcoded in FML and can not be changed.
    // https://github.com/neoforged/FancyModLoader/blob/a952595eaaddd571fbc53f43847680b00894e0c1/loader/src/main/java/net/neoforged/fml/loading/moddiscovery/ModFile.java#L118
    val transformerFile = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (transformerFile.exists())
        accessTransformers.from(transformerFile)

    parchment {
        minecraftVersion = parchmentMinecraft
        mappingsVersion = parchmentVersion
    }
}

dependencies {
    val forgeConfigAPIVersion: String by project

    api("fuzs.forgeconfigapiport:forgeconfigapiport-common-neoforgeapi:$forgeConfigAPIVersion")
    compileOnly("org.spongepowered:mixin:0.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

configurations {
    create("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    create("commonKotlin") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    create("commonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets.main.get().java.sourceDirectories.singleFile)
    add("commonKotlin", sourceSets.main.get().kotlin.sourceDirectories.filter { !it.name.endsWith("java") }.singleFile)
    add("commonResources", sourceSets.main.get().resources.sourceDirectories.singleFile)
}