# Cursor Setup for AstralBot

## Quick Setup for Cursor

### 1. Install Cursor
- Download from: https://cursor.sh/
- Install and launch Cursor

### 2. Open the Project
```bash
# In Cursor, open the AstralBot directory
File → Open Folder → /home/boss/IdeaProjects/AstralBot
```

### 3. Essential Extensions for Cursor

Install these extensions via the Extensions panel (Ctrl+Shift+X):

#### Core Extensions
- **Kotlin Language** (`mathiasfrohlich.Kotlin`)
- **Kotlin Extension Pack** (`fwcd.kotlin`)
- **Gradle for Java** (`vscjava.vscode-gradle`)
- **Java Extension Pack** (`vscjava.vscode-java-pack`)

#### Development Tools
- **GitLens** (`eamodio.gitlens`) - Enhanced Git integration
- **Bracket Pair Colorizer 2** (`CoenraadS.bracket-pair-colorizer-2`)
- **Material Icon Theme** (`PKief.material-icon-theme`)
- **SonarLint** (`SonarSource.sonarlint-vscode`) - Code quality

### 4. Cursor Settings

The project already includes optimized settings in `.vscode/settings.json`:
- Java 17 configuration
- Kotlin language server enabled
- Auto-formatting on save
- Build directories excluded from search

### 5. AI Assistant Configuration

Cursor's AI is already configured for:
- **Kotlin development** - Excellent Kotlin code suggestions
- **Minecraft modding** - Understands Minecraft APIs
- **Discord bot development** - JDA library knowledge
- **Gradle builds** - Build system assistance

### 6. Quick Commands

```bash
# Setup environment
./setup.sh

# Build project
./gradlew build

# Run specific modules
./gradlew :fabric:build
./gradlew :forge:build
```

### 7. Cursor AI Tips

- **Ask for code explanations**: "Explain this Minecraft mixin"
- **Request refactoring**: "Refactor this Discord command handler"
- **Get debugging help**: "Why is this Kotlin coroutine not working?"
- **Ask for best practices**: "What's the best way to handle Minecraft events?"

### 8. Project Structure in Cursor

```
AstralBot/
├── common/           # Shared Kotlin code
├── fabric/           # Fabric-specific code
├── forge/            # Forge-specific code
├── .vscode/          # Cursor/VS Code settings
└── gradle/           # Build configuration
```

### 9. Next Steps

1. **Open the project** in Cursor
2. **Install the extensions** listed above
3. **Run the setup script**: `./setup.sh`
4. **Build the project**: `./gradlew build`
5. **Start coding** with AI assistance!

The project is now ready for development in Cursor with full AI support for Kotlin, Minecraft modding, and Discord bot development.





