# IDE Plugins for AstralBot Development

This guide provides recommendations for plugins that will enhance your development experience with the AstralBot project.

## IntelliJ IDEA (Recommended)

IntelliJ IDEA is the most popular IDE for Kotlin development and provides excellent support for Minecraft modding.

### Essential Plugins

#### 1. **Kotlin Plugin** (Built-in)
- **Purpose**: Core Kotlin language support
- **Features**: Syntax highlighting, code completion, refactoring, debugging
- **Status**: ✅ Usually pre-installed

#### 2. **Gradle Plugin** (Built-in)
- **Purpose**: Gradle build system integration
- **Features**: Build tool window, dependency management, task execution
- **Status**: ✅ Usually pre-installed

#### 3. **Minecraft Development**
- **ID**: `com.demonwav.minecraft-dev`
- **Purpose**: Minecraft mod development support
- **Features**: 
  - Minecraft-specific code completion
  - Mixin support
  - Resource pack development
  - Mod metadata validation
- **Installation**: 
  1. Go to `File` → `Settings` → `Plugins`
  2. Search for "Minecraft Development"
  3. Install and restart IDE

#### 4. **Discord Integration**
- **ID**: `com.discord.intellij`
- **Purpose**: Discord integration for development
- **Features**: 
  - Discord presence integration
  - Rich presence in IDE
- **Installation**: Available in JetBrains Marketplace

#### 5. **Rainbow Brackets**
- **ID**: `izhangzhihao.rainbow.brackets`
- **Purpose**: Visual bracket matching
- **Features**: Color-coded brackets for better code readability
- **Installation**: Available in JetBrains Marketplace

#### 6. **GitToolBox**
- **ID**: `com.github.gitToolbox`
- **Purpose**: Enhanced Git integration
- **Features**: 
  - Advanced Git operations
  - Branch management
  - Commit message templates
- **Installation**: Available in JetBrains Marketplace

#### 7. **String Manipulation**
- **ID**: `com.wix_ss.string.manipulation`
- **Purpose**: String manipulation utilities
- **Features**: 
  - Case conversion
  - String formatting
  - Text transformation
- **Installation**: Available in JetBrains Marketplace

#### 8. **Key Promoter X**
- **ID**: `com.halirutan.KeyPromoterX`
- **Purpose**: Keyboard shortcut learning
- **Features**: 
  - Shows keyboard shortcuts for actions
  - Helps learn IDE shortcuts faster
- **Installation**: Available in JetBrains Marketplace

### Recommended Plugins

#### 9. **Material Theme UI**
- **ID**: `com.mallowigi.idea.MaterialThemeUI`
- **Purpose**: Modern UI theme
- **Features**: 
  - Material Design theme
  - Dark/light mode support
  - Customizable colors
- **Installation**: Available in JetBrains Marketplace

#### 10. **Atom Material Icons**
- **ID**: `com.mallowigi.idea.AtomMaterialIcons`
- **Purpose**: File type icons
- **Features**: 
  - Material Design file icons
  - Better visual file organization
- **Installation**: Available in JetBrains Marketplace

#### 11. **SonarLint**
- **ID**: `com.sonarlint.idea`
- **Purpose**: Code quality analysis
- **Features**: 
  - Real-time code quality feedback
  - Bug detection
  - Code smell identification
- **Installation**: Available in JetBrains Marketplace

#### 12. **GitHub Copilot**
- **ID**: `com.github.copilot`
- **Purpose**: AI code completion
- **Features**: 
  - AI-powered code suggestions
  - Context-aware completions
- **Installation**: Available in JetBrains Marketplace (requires subscription)

## Visual Studio Code

VS Code is a lightweight, extensible editor that's great for Kotlin development.

### Essential Extensions

#### 1. **Kotlin Language**
- **ID**: `mathiasfrohlich.Kotlin`
- **Purpose**: Kotlin language support
- **Features**: Syntax highlighting, IntelliSense
- **Installation**: VS Code Marketplace

#### 2. **Kotlin Extension Pack**
- **ID**: `fwcd.kotlin`
- **Purpose**: Comprehensive Kotlin support
- **Features**: 
  - Language server
  - Code completion
  - Refactoring
  - Debugging
- **Installation**: VS Code Marketplace

#### 3. **Gradle for Java**
- **ID**: `vscjava.vscode-gradle`
- **Purpose**: Gradle integration
- **Features**: 
  - Gradle task execution
  - Dependency management
  - Build tool integration
- **Installation**: VS Code Marketplace

#### 4. **Java Extension Pack**
- **ID**: `vscjava.vscode-java-pack`
- **Purpose**: Java development support
- **Features**: 
  - Java language server
  - Debugging
  - Testing
- **Installation**: VS Code Marketplace

#### 5. **Minecraft Development**
- **ID**: `ms-vscode.vscode-minecraft`
- **Purpose**: Minecraft development support
- **Features**: 
  - Minecraft-specific tooling
  - Resource pack support
- **Installation**: VS Code Marketplace

### Recommended Extensions

#### 6. **GitLens**
- **ID**: `eamodio.gitlens`
- **Purpose**: Enhanced Git integration
- **Features**: 
  - Git blame information
  - File history
  - Branch comparison
- **Installation**: VS Code Marketplace

#### 7. **Bracket Pair Colorizer 2**
- **ID**: `CoenraadS.bracket-pair-colorizer-2`
- **Purpose**: Visual bracket matching
- **Features**: Color-coded brackets
- **Installation**: VS Code Marketplace

#### 8. **Material Icon Theme**
- **ID**: `PKief.material-icon-theme`
- **Purpose**: Material Design file icons
- **Features**: Better visual file organization
- **Installation**: VS Code Marketplace

#### 9. **One Dark Pro**
- **ID**: `zhuangtongfa.Material-theme`
- **Purpose**: Dark theme
- **Features**: Modern dark theme
- **Installation**: VS Code Marketplace

#### 10. **SonarLint**
- **ID**: `SonarSource.sonarlint-vscode`
- **Purpose**: Code quality analysis
- **Features**: Real-time code quality feedback
- **Installation**: VS Code Marketplace

## Eclipse

Eclipse is another popular IDE for Java/Kotlin development.

### Essential Plugins

#### 1. **Kotlin Plugin for Eclipse**
- **Purpose**: Kotlin language support
- **Installation**: Eclipse Marketplace

#### 2. **Gradle Integration**
- **Purpose**: Gradle build system support
- **Installation**: Eclipse Marketplace

#### 3. **EGit**
- **Purpose**: Git integration
- **Installation**: Eclipse Marketplace

## Installation Commands

### IntelliJ IDEA (Command Line)
```bash
# Install plugins via command line (if available)
# Note: Most plugins need to be installed via the IDE interface
```

### VS Code (Command Line)
```bash
# Install essential extensions
code --install-extension mathiasfrohlich.Kotlin
code --install-extension fwcd.kotlin
code --install-extension vscjava.vscode-gradle
code --install-extension vscjava.vscode-java-pack
code --install-extension eamodio.gitlens
code --install-extension CoenraadS.bracket-pair-colorizer-2
code --install-extension PKief.material-icon-theme
code --install-extension SonarSource.sonarlint-vscode
```

## Project-Specific Configuration

### IntelliJ IDEA Settings

1. **Set Project SDK to Java 17**:
   - Go to `File` → `Project Structure`
   - Set Project SDK to Java 17
   - Set Project language level to 17

2. **Configure Kotlin**:
   - Go to `File` → `Settings` → `Languages & Frameworks` → `Kotlin`
   - Ensure Kotlin compiler is properly configured

3. **Import Gradle Project**:
   - Go to `File` → `Open`
   - Select the project directory
   - Choose "Import project from external model" → Gradle

### VS Code Settings

Create `.vscode/settings.json`:
```json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic",
    "kotlin.languageServer.enabled": true,
    "files.associations": {
        "*.gradle.kts": "kotlin"
    }
}
```

## Recommended Workflow

1. **Install IntelliJ IDEA** (Community or Ultimate)
2. **Install essential plugins** listed above
3. **Import the project** as a Gradle project
4. **Configure Java 17** as the project SDK
5. **Set up Git integration** for version control
6. **Configure Minecraft Development plugin** for mod-specific features

## Troubleshooting

### Common Issues

**Plugin not found**:
- Ensure you're using the correct plugin ID
- Check if the plugin is available for your IDE version
- Try searching with different keywords

**Kotlin language server not working**:
- Restart the IDE after installing Kotlin plugin
- Check if Java 17 is properly configured
- Verify Gradle sync completed successfully

**Minecraft Development plugin issues**:
- Ensure you have the latest version
- Check if the plugin supports your Minecraft version (1.18.2)
- Restart IDE after installation

## Next Steps

1. **Choose your preferred IDE** (IntelliJ IDEA recommended)
2. **Install the essential plugins** for your chosen IDE
3. **Configure the project** according to the settings above
4. **Test the setup** by building the project
5. **Start developing** with enhanced tooling support

---

**Note**: Plugin availability may vary depending on your IDE version and operating system. Always check the official plugin repositories for the most up-to-date information.
