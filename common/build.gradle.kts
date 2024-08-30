plugins {
    idea
    java
    id("net.neoforged.moddev")
}

val neoformVersion: String by project
val minecraftVersion: String by project
val mcVersion = minecraftVersion
val parchmentVersion: String by project
val parchmentMinecraft: String by project
val modId: String by project

neoForge {
    neoFormVersion = neoformVersion

    parchment {
        minecraftVersion = parchmentMinecraft
        mappingsVersion = parchmentVersion
    }
}

dependencies {
    val forgeConfigAPIVersion: String by project

    api("fuzs.forgeconfigapiport:forgeconfigapiport-common-neoforgeapi:$forgeConfigAPIVersion")
    compileOnly("org.spongepowered:mixin:0.8.7")
}