import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.20"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application.mainClass = "dev.erdragh.erdbot.BotKt"
group = "dev.erdragh"
version = "1.0-SNAPSHOT"
val jdaVersion = "5.0.0-beta.18"

repositories {
    mavenCentral()
}

dependencies {
//    testImplementation(kotlin("test"))
    implementation("net.dv8tion:JDA:${jdaVersion}")
    implementation("ch.qos.logback:logback-classic:1.2.9")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}