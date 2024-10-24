# AstralBot
[![build workflow](https://img.shields.io/github/actions/workflow/status/Erdragh/AstralBot/build.yml?style=for-the-badge)](https://github.com/Erdragh/AstralBot/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/Juuxel/Adorn.svg?style=for-the-badge)](LICENSE)
![Environment](https://img.shields.io/badge/environment-server-4caf50?style=for-the-badge)

This is a Minecraft mod and Discord Bot in one package. It's intended for the [Create: Astral](https://www.curseforge.com/minecraft/modpacks/create-astral)
Modpack's official server, but it's implemented cross-platform to be usable by others too.
The goal is to be easy-to-use and accessible for non-technical server admins,
while allowing true complexity for power users.

## Features
These features are the core of what this bot will do. See [the Status section](#status)
to see how much is already implemented.
- Discord and Minecraft account linking, optionally requiring this to be whitelisted
- Discord and Minecraft chat synchronization, optionally via webhook for improved readability
- FAQ commands using Markdown files without needing to restart the server

## Dependencies

This mod has a few dependencies, some of which are not specified directly as they're technically optional:
- The Kotlin Implementation for the platform you're running. (e.g. [Kotlin For Forge](https://modrinth.com/mod/kotlin-for-forge) or [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin))
- The (Neo-)Forge Config API (Included in Neo/Forge, [extra mod for Fabric](https://modrinth.com/mod/forge-config-api-port))

## Implementation
- [JDA](https://jda.wiki) library to communicate with the Discord API.
- Kotlin for the improved development experience over Java
- [JetBrains Exposed](https://github.com/JetBrains/Exposed) to communicate with the Database

## Configuration
The following things will be configurable:
- Required linking to be whitelisted (default: `off`)
- Chat synchronization with imitated Discord Users:
  1. No user imitation (The bot will write the messages under its own name)
  2. (If possible) Minecraft user imitation (The Minecraft usernames and heads will be used for messages)
  3. (Only available if requiring linking for whitelist) Discord user imitation (The messages will be written under the linked Discord name)
  
  Default: Minecraft user imitation (if webhook available is set, otherwise no imitation)
- Managing FAQs through a command interface (default: `off`)
- Database connection. Uses an SQLite database by default

## Status
- [x] Whitelisting
- [ ] FAQ Commands
  - [x] Reading Markdown files
  - [x] Updating suggestions without restart
  - [ ] Management/Creation via commands
- [x] Chat synchronization
  - [x] Minecraft to Discord
  - [x] Discord to Minecraft
  - [x] User imitation on Discord. Either uses Minecraft avatars or Discord avatars

## Running
There is no public instance of this bot/mod available. To use it, create a new Application
on the [Discord Developer Portal](https://discord.com/developers/applications) and configure it
to have the three privileged gateway intents: `PRESENCE`, `SERVER MEMBERS` and `MESSAGE CONTENT`.

Copy the bot token and store it somewhere safe (like a Password Manager) and never show it to
anybody else.

The bot can read the token from multiple sources, the easiest to configure being the `token` field in the
main server config file (`astralbot-server.toml`). Alternatively, you can also provide it via an [Environment Variable](https://en.wikipedia.org/wiki/Environment_variable)
`DISCORD_TOKEN` where the running Minecraft server can access it.
You could for example modify a `start.sh` script on a Unix-like system to `export` it or start the shell script with it set directly:

`startmc.sh`:
```shell
export DISCORD_TOKEN=<place token here>

java ... # java command that starts the server
```

or

```shell
DISCORD_TOKEN=<place token here> startmc.sh # Start the script with the env variable set
```

After starting the server, you can go into the OAuth2 URL builder on the Discord
Developer Portal and generate a URL with the `bot` and `applications.command` permissions.
Use the generated URL to have the bot join your server.

To be able to use user imitation, which makes the chat synchronization more readable, the bot will try and create a webhook
in the configured chat synchronization channel. This means it will need to have the permission to manage webhooks. If you
don't want that you can manually create a webhook in the channel where you want the messages synchronized and set it in the
`webhook.url` option in the config for the mod.