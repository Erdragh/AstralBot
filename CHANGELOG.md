# 1.5.3
- Unlink users on leave
- Update Dependencies
- Show better error message if FAQ is too long
- Limit the autocomplete suggestion return value length to fix no options showing up
- Added the `/listfaq` command

# 1.5.2
Fix tps command using wrong numbers for calculation

# 1.5.1
Hotfix for Versions 1.18.2 - 1.20.1 fixing missing classes

# 1.5.0
- Introduce webhook chat sync, allowing for user imitation
  
  **IMPORTANT INFO**: If you don't want to manually set the webhook URL, the
  bot needs to have the MANAGE_WEBHOOKS permission

# 1.4.0
- Port to 1.20.4 and NeoForge
- Removed Forge as a supported Platform for Minecraft > 1.20.1
- JiJ SQLite and Commonmark dependencies, allowing better compatibility and removing the SQLite JDBC dependency
- Handle messages sent by commands too (i.e. `say hello`)

# 1.3.0
- Message Embeds to better represent Minecraft messages on Discord
- Fix `/link` Minecraft command not being registered on Forge
- Implement Markdown parsing for Discord to Minecraft message sync
- Implement automatically converting URLs to clickable links in Minecraft chat
- Replace blocked URLs before sending Minecraft chat to Discord

# 1.2.1
Allow providing token via config

# 1.2.0
- Unlink other Discord users with permissions
- Customizable text for basically anything
- `/headpat` command
- More graceful shutdown
- `/link` as a Minecraft command for people who want to be linked but are already whitelisted by another way
- Exception handling for commands server side, resulting in less commands that don't get a response

# 1.1.0
- Management Commands:
    - `/tps`
    - `/usage`
    - `/stop`
    - `/uptime`
- Stop Minecraft usernames being formatted as Markdown on Discord
- Rework enabled commands config

# 1.0.2
- Solve Compatibility issue with [GML](https://modrinth.com/mod/gml) on Forge
- Fix empty whitelist Database handling

# 1.0.1
- Config option to disable `/unlink`
- Async bot startup
- Make `/reload` command remove no longer registered commands
- Update dependencies
- Make SQLite driver an external dependency, allowing compatibility with [Ledger](https://modrinth.com/mod/ledger)

# 1.0.0
Initial Release