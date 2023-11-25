import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.20"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application.mainClass = "dev.erdragh.erdbot.BotKt"
group = "dev.erdragh"
version = "1.0-SNAPSHOT"
val jdaVersion: String by project
val exposedVersion: String by project
val sqliteJDBCVersion: String by project
val logbackVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
//    testImplementation(kotlin("test"))
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-money:$exposedVersion")

    implementation("org.xerial:sqlite-jdbc:$sqliteJDBCVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}