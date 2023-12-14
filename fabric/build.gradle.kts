architectury {
    fabric()
}

val common: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
    configurations.runtimeClasspath.get().extendsFrom(this)
    configurations["developmentFabric"].extendsFrom(this)
}

dependencies {
    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    val minecraftVersion: String by project
    val fabricLoaderVersion: String by project
    val fabricApiVersion: String by project
    val modMenuVersion: String by project
    val fabricKotlinVersion: String by project
    val forgeConfigAPIVersion: String by project
    val nightConfig: String by project

    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = fabricLoaderVersion)
    modApi(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "$fabricApiVersion+$minecraftVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")

    modApi(group = "com.terraformersmc", name = "modmenu", version = modMenuVersion)

    //modApi("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:$forgeConfigAPIVersion")
    modApi("com.electronwill.night-config:core:$nightConfig")
    modApi("com.electronwill.night-config:toml:$nightConfig")
    modApi("net.minecraftforge:forgeconfigapiport-fabric:$forgeConfigAPIVersion")
}

// Fixes "duplicate fabric loader classes found on classpath" error
configurations.configureEach {
    resolutionStrategy.eachDependency {
        val fabricLoaderVersion: String by project

        if (requested.module.name == "fabric-loader") {
            useVersion(fabricLoaderVersion)
        }
    }
}