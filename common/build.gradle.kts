architectury {
    val enabledPlatforms: String by rootProject
    common(enabledPlatforms.split(","))
}

dependencies {
    val fabricLoaderVersion: String by project
    val forgeConfigAPIVersion: String by project
    val nightConfig: String by project
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    //api("fuzs.forgeconfigapiport:forgeconfigapiport-common:$forgeConfigAPIVersion")
    api("com.electronwill.night-config:core:$nightConfig")
    api("com.electronwill.night-config:toml:$nightConfig")
    api("net.minecraftforge:forgeconfigapiport-fabric:$forgeConfigAPIVersion")
}