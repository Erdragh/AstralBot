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