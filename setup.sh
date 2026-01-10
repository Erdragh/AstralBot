#!/bin/bash

# AstralBot Setup Script
# This script sets up the development environment for the AstralBot project

echo "🚀 Setting up AstralBot development environment..."

# Set Java 17 as the default for this project
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

echo "✅ Java environment configured:"
java -version

echo ""
echo "📋 Available commands:"
echo "  ./gradlew build          - Build the entire project"
echo "  ./gradlew :common:build  - Build only the common module"
echo "  ./gradlew :fabric:build  - Build only the Fabric module"
echo "  ./gradlew :forge:build   - Build only the Forge module"
echo "  ./gradlew runClient      - Run the client (Fabric)"
echo "  ./gradlew runServer      - Run the server (Fabric)"
echo "  ./gradlew clean          - Clean build artifacts"
echo ""
echo "🔧 Project Information:"
echo "  - Language: Kotlin 2.1.10"
echo "  - Java Version: 17"
echo "  - Minecraft Version: 1.18.2"
echo "  - Supported Loaders: Fabric, Forge"
echo "  - Discord Bot: JDA 5.1.2"
echo "  - Database: SQLite with Exposed ORM"
echo ""
echo "📁 Project Structure:"
echo "  common/     - Shared code for both Fabric and Forge"
echo "  fabric/     - Fabric-specific implementation"
echo "  forge/      - Forge-specific implementation"
echo ""
echo "🎯 Next Steps:"
echo "  1. Create a Discord application at https://discord.com/developers/applications"
echo "  2. Get your bot token and configure it in the config file"
echo "  3. Build and test the mod"
echo ""
echo "💡 Tip: Run 'source setup.sh' to set up the environment in your current shell"
