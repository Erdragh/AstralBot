#!/bin/bash

# Cursor Extensions Installation Script for AstralBot
echo "🚀 Installing Cursor extensions for AstralBot development..."

# Check if Cursor is installed
if ! command -v cursor &> /dev/null; then
    echo "❌ Cursor is not installed or not in PATH"
    echo "Please install Cursor first: https://cursor.sh/"
    exit 1
fi

# Core extensions for Kotlin development
echo "📦 Installing Kotlin extensions..."
cursor --install-extension mathiasfrohlich.Kotlin
cursor --install-extension fwcd.kotlin

# Java and Gradle support
echo "📦 Installing Java and Gradle extensions..."
cursor --install-extension vscjava.vscode-gradle
cursor --install-extension vscjava.vscode-java-pack

# Development tools
echo "📦 Installing development tools..."
cursor --install-extension eamodio.gitlens
cursor --install-extension CoenraadS.bracket-pair-colorizer-2
cursor --install-extension PKief.material-icon-theme
cursor --install-extension SonarSource.sonarlint-vscode

echo ""
echo "✅ Cursor extensions installation complete!"
echo ""
echo "🎯 Next steps:"
echo "  1. Open Cursor in the project directory"
echo "  2. Run: ./setup.sh"
echo "  3. Run: ./gradlew build"
echo "  4. Start coding with AI assistance!"
