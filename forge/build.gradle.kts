import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("multiloader-loader")
    alias(libs.plugins.moddev)
}

val modId: String by project

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}.mixins.json")
    config("${modId}.forge.mixins.json")
}
tasks.jar {
    manifest {
        attributes["MixinConfigs"] = "${modId}.mixins.json,${modId}.forge.mixins.json"
    }
}

legacyForge {
    version = libs.versions.forge.get()
    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = libs.versions.parchmentMC.get()
        mappingsVersion = libs.versions.parchment.get()
    }
    runs {
        configureEach {
            systemProperty("forge.enabledGameTestNamespaces", modId)
            ideName = "Forge ${name.capitalized()} (${project.path})" // Unify the run config names with fabric
        }
        register("client") {
            client()
        }
        register("data") {
            data()
        }
        register("server") {
            server()
        }
    }
    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main.get().resources { srcDir("src/generated/resources") }

dependencies {
    implementation(libs.kff)
    annotationProcessor(variantOf(libs.mixin) { classifier("processor") })

    configurations.named("additionalRuntimeClasspath").extendsFrom(configurations.botLib)
    configurations.jarJar.extendsFrom(configurations.botLib)
}