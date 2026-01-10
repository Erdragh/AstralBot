# AstralBot Setup Guide

This guide will help you set up the AstralBot development environment and get the project running.

## Prerequisites

### Required Software
- **Java 17** (OpenJDK or Oracle JDK)
- **Gradle** (included via wrapper)
- **Git** (for version control)

### System Requirements
- Linux, macOS, or Windows
- At least 4GB RAM (8GB recommended)
- 2GB free disk space

## Quick Setup

1. **Clone the repository** (if you haven't already):
   ```bash
   git clone <repository-url>
   cd AstralBot
   ```

2. **Run the setup script**:
   ```bash
   ./setup.sh
   ```

3. **Build the project**:
   ```bash
   ./gradlew build
   ```

## Manual Setup

If you prefer to set up manually or the setup script doesn't work:

### 1. Configure Java 17

**Linux/macOS:**
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

**Windows:**
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
```

### 2. Verify Java Version
```bash
java -version
```
Should show Java 17.x.x

### 3. Build the Project
```bash
./gradlew build
```

## Project Structure

```
AstralBot/
├── common/           # Shared code for both Fabric and Forge
├── fabric/           # Fabric-specific implementation
├── forge/            # Forge-specific implementation
├── buildSrc/         # Gradle build logic
├── gradle/           # Gradle wrapper and version catalogs
├── setup.sh          # Setup script
├── astralbot-server.toml.example  # Configuration template
└── README.md         # Project documentation
```

## Configuration

### 1. Discord Bot Setup

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application
3. Go to the "Bot" section and create a bot
4. Enable these privileged intents:
   - Presence Intent
   - Server Members Intent
   - Message Content Intent
5. Copy the bot token

### 2. Configuration File

1. Copy the example configuration:
   ```bash
   cp astralbot-server.toml.example astralbot-server.toml
   ```

2. Edit `astralbot-server.toml` and fill in:
   - Your Discord bot token
   - Discord server (guild) ID
   - Discord channel ID for chat sync

### 3. Environment Variables (Alternative)

You can also set the bot token via environment variable:
```bash
export DISCORD_TOKEN=your_bot_token_here
```

## Building and Running

### Build Commands

```bash
# Build entire project
./gradlew build

# Build specific modules
./gradlew :common:build
./gradlew :fabric:build
./gradlew :forge:build

# Clean build artifacts
./gradlew clean
```

### Running the Mod

**Fabric:**
```bash
# Run client
./gradlew :fabric:runClient

# Run server
./gradlew :fabric:runServer
```

**Forge:**
```bash
# Run client
./gradlew :forge:runClient

# Run server
./gradlew :forge:runServer
```

## Development

### Key Technologies

- **Kotlin 2.1.10** - Primary programming language
- **Java 17** - Runtime environment
- **Minecraft 1.18.2** - Target game version
- **Fabric/Forge** - Mod loaders
- **JDA 5.1.2** - Discord API library
- **Exposed ORM** - Database access
- **SQLite** - Database storage

### IDE Setup

**IntelliJ IDEA (Recommended):**
1. Open the project
2. Import Gradle project
3. Set project SDK to Java 17
4. Install Kotlin plugin if not already installed

**VS Code:**
1. Install Kotlin extension
2. Install Java extension pack
3. Open the project folder

### Code Style

The project uses the official Kotlin code style. Configure your IDE to use:
- Official Kotlin code style
- 4-space indentation
- UTF-8 encoding

## Troubleshooting

### Common Issues

**Build fails with Java version error:**
- Ensure you're using Java 17
- Run `./setup.sh` to configure the environment

**Gradle daemon issues:**
```bash
./gradlew --stop
./gradlew clean build
```

**Discord bot not connecting:**
- Verify bot token is correct
- Check that all required intents are enabled
- Ensure bot has proper permissions in the Discord server

**Mod not loading:**
- Check Minecraft version compatibility (1.18.2)
- Verify all dependencies are installed
- Check server logs for error messages

### Getting Help

- Check the [README.md](README.md) for project overview
- Review the [CHANGELOG.md](CHANGELOG.md) for recent changes
- Open an issue on the project repository

## Next Steps

1. **Test the build** - Ensure everything compiles correctly
2. **Configure Discord** - Set up your bot and configuration
3. **Test the mod** - Run it in a Minecraft server
4. **Customize** - Modify features according to your needs

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

Happy coding! 🚀
