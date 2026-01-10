# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AstralBot is a Minecraft mod and Discord bot hybrid written in Kotlin that bridges communication between Minecraft servers and Discord communities. It features a multi-loader architecture supporting both Fabric and Forge for Minecraft 1.18.2.

## Common Commands

### Build System
```bash
./gradlew build                    # Build all modules
./gradlew publishMods             # Publish to Modrinth and CurseForge
./gradlew fabric:build            # Build Fabric version only
./gradlew forge:build             # Build Forge version only
./gradlew tasks                   # List all available tasks
```

### Development
```bash
./gradlew fabric:runClient        # Run Fabric client
./gradlew fabric:runServer        # Run Fabric server
./gradlew forge:runClient         # Run Forge client
./gradlew forge:runServer         # Run Forge server
```

## Architecture

The project uses a multi-loader architecture with three main modules:

- **common/**: Core business logic and shared code in Kotlin
- **fabric/**: Fabric-specific implementations and event handling
- **forge/**: Forge-specific implementations and event handling

### Key Components

- `Bot.kt` - Main bot orchestrator and lifecycle management
- `MinecraftHandler` - Handles Minecraft ↔ Discord communication
- `FAQHandler` - Manages FAQ system with file watching
- `WhitelistHandler` - Discord-based whitelist management
- `commands/discord/` - Discord slash commands
- `commands/minecraft/` - Minecraft server commands

### Technology Stack

- **Language**: Kotlin 2.1.10 with Java 17
- **Discord API**: JDA v5.1.2
- **Database**: SQLite with JetBrains Exposed ORM
- **Configuration**: Forge Config API (cross-platform)
- **Documentation**: CommonMark for Markdown parsing

## Configuration

Main configuration files:
- `astralbot-server.toml` - Bot configuration
- `astralbot-text.toml` - Customizable text messages
- `gradle.properties` - Version and build configuration

Environment variables:
- `DISCORD_TOKEN` - Discord bot token (required)

## Dependency Management

The project uses Gradle version catalogs for organized dependency management:
- `libs.versions.toml` - Core dependencies
- `gradle/jda.versions.toml` - Discord API dependencies
- `gradle/dcwebhooks.versions.toml` - Webhook dependencies
- `gradle/exposed.versions.toml` - Database dependencies

## Development Notes

- All Discord API dependencies are bundled into the mod
- Use coroutines for async operations
- Platform-specific code goes in respective `fabric/` or `forge/` modules
- Shared logic belongs in `common/` module
- FAQ files support live reloading without server restart
- Database connections are configurable but default to SQLite

## Branch Strategy

- `develop` - Main development branch
- `main` - Stable releases
- `version/*` - Version-specific branches