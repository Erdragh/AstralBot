plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("changelogExtractor") {
            id = "dev.erdragh.changelog-extractor"
            implementationClass = "dev.erdragh.changelog.ChangelogExtractorPlugin"
        }
    }
}