# OmniBridge

**A Paper plugin that downloads, configures, and manages the plugins needed for cross-version and cross-platform Minecraft support.**

[![License: GPL-3.0](https://img.shields.io/github/license/enis1enis2/OmniBridge)](https://www.gnu.org/licenses/gpl-3.0)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00)](https://openjdk.org/projects/jdk/21/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20%2B-00AA00)](https://www.minecraft.net)

---

## Features

- Automatic downloading, configuration, and management of **27** cross-version, cross-platform, and integration plugins
- Interactive chat-based configuration menu for guided setup
- 7 plugin presets for quick deployment (`bedrock`, `full-version`, `essentials`, `server-essentials`, `max-compat`, `performance`, `chat`)
- Localized chat messages with **72 locale files** and per-key fallback to `en_US`
- Automatic dependency resolution across managed plugins
- External API for integration with third-party plugins
- Real-time player version tracking via ViaVersion, and Bedrock player identification via Geyser
- Safe installs and updates: downloads are written to a temp file, validated, and atomically moved into place
- Centralized update checking with three states: update available, up to date, or unknown

---

## Quick Start

### Requirements

- **Java 21 or later**
- **Paper 1.20 or newer** (the plugin targets `api-version: 1.20` and uses the Adventure API)
- An internet connection if you want OmniBridge to download plugins for you

OmniBridge is a **Paper plugin**. It is not a Spigot, Folia, or Fabric mod. The JAR also
contains a minimal Velocity entry point (see [Velocity](#velocity)) but plugin management
is Paper-only.

### Installation

1. Download the latest `OmniBridge-1.0.0.jar` from the [Releases](https://github.com/enis1enis2/OmniBridge/releases) page.
2. Place the JAR file into your server's `plugins/` directory.
3. Restart your server.
4. Run `/ob setup` in-game or from the console to launch the interactive configuration menu.
5. Restart the server once more so the newly downloaded plugins are loaded.

---

## Commands

All commands are available as `/omnibrige` with the aliases `/ob` and `/omnib`.

| Command | Description |
|---|---|
| `/ob setup` | Launch the interactive chat-based configuration menu |
| `/ob toggle <plugin>` | Enable or disable a specific managed plugin |
| `/ob preset [name]` | Apply a configuration preset group |
| `/ob install` | Download all currently enabled plugins |
| `/ob update` | Check for and download updates, including plugins whose version cannot be compared |
| `/ob check` | Show update state for each installed plugin (`UPDATE AVAILABLE`, `up to date`, or `status unknown`) |
| `/ob status` | Show the current status of all managed plugins |
| `/ob info <plugin>` | Show version, install target, and description for one plugin |
| `/ob versions` | List connected player versions (includes Bedrock tags) |
| `/ob reload` | Reload the config and locale, then reload all managed plugins |
| `/ob remove <plugin>` | Uninstall a managed plugin from the server |
| `/ob enable-all` | Enable every managed plugin |
| `/ob disable-all` | Disable every managed plugin |
| `/ob help` | Display available commands and usage information |

Tab completion is available for subcommands and plugin names.

---

## Configuration

OmniBridge is configured through `plugins/OmniBridge/config.yml`:

| Option | Description |
|---|---|
| `auto-install` | Automatically download enabled plugins on startup |
| `auto-update` | Automatically apply updates on startup |
| `managed-plugins` | Per-plugin enable/disable toggles (all disabled by default) |
| `bedrock.address` / `bedrock.port` | Address and port Geyser should listen on for Bedrock clients |
| `java.address` / `java.port` | Address and port of the Java server Geyser connects to |
| `locale` | Locale code for chat messages, e.g. `en_US` or `tr_TR` |
| `debug` | Enable verbose logging for troubleshooting |
| `reminder.interval-minutes` | How often to remind ops that no managed plugins are enabled (minimum 1) |

Use `/ob setup` to configure these options interactively, or edit the file directly and run
`/ob reload`.

### Plugin management model

- A plugin is downloaded only when its `managed-plugins` entry is `true`.
- Dependencies are resolved automatically. Enabling Geyser also enables Floodgate; enabling
  AuthMe also enables Floodgate; DiscordSRV pulls in PlaceholderAPI; TuffXPlus pulls in
  ViaVersion and ViaBackwards.
- Platform-restricted entries (`viabungee`, `protocolib`, `tuffxplus`) are skipped when the
  current platform does not support them.
- Unknown keys in `managed-plugins` are ignored with a warning instead of failing.

---

## Managed Plugins

### ViaVersion Family (Cross-Version Support)

| Plugin | Description |
|---|---|
| ViaVersion | Allows newer Minecraft clients to connect to older servers |
| ViaBackwards | Allows older Minecraft clients to connect to newer servers |
| ViaRewind | Provides backward compatibility for 1.7.x/1.8.x clients |
| ViaRewindLegacySupport | Extra block and entity fixes for ViaRewind (Paper only) |
| ViaAprilFools | Protocol mappings for April Fools snapshots |
| ViaBungee | ViaVersion support for Waterfall/BungeeCord proxies (Velocity-only entry) |

### GeyserMC Family (Cross-Platform / Bedrock Support)

| Plugin | Description | Installed to |
|---|---|---|
| Geyser | Lets Bedrock Edition clients join a Java server | `plugins/` |
| Floodgate | Bedrock authentication without a Java account | `plugins/` |
| Hurricane | Server-side workarounds for Geyser players | `plugins/` |
| GeyserConnect | Lets Bedrock players join without a proxy | `plugins/Geyser-Spigot/extensions/` |
| ThirdPartyCosmetics | Third-party cosmetic support for Bedrock clients | `plugins/Geyser-Spigot/extensions/` |

> **Out of scope:** Rainbow (a Fabric client mod) and ThunderBeta (a resource pack
> converter) are not server plugins and are not managed by OmniBridge.

### Integration

| Plugin | Description |
|---|---|
| AuthMe | Registration, login, and authentication management |
| TAB | Player list formatting, prefixes, suffixes, and header/footer customization |
| LuckPerms | Permissions management |
| EssentialsX | General server commands and utilities |
| PlaceholderAPI | Placeholder expansion API used by many plugins |
| WorldGuard | Region protection and flags |
| CoreProtect | Block and container logging/rollback |
| DiscordSRV | Bridges Minecraft chat with Discord |
| Vault | Economy/permission abstraction layer |

### Community

| Plugin | Description |
|---|---|
| ProtocolLib | Packet-level API used by many plugins for protocol manipulation |
| TuffXPlus | Modern blocks and entities for TuffClient |
| spark | CPU/memory profiling and server health reporting |
| Chunky | Chunk pre-generation |
| BlueMap | 3D web map renderer for Minecraft worlds |
| GriefPrevention | Land claims and anti-grief protection |
| CommandAPI | Command registration API for newer Minecraft versions |

---

## Update Checking

`/ob check` reports one of three states per installed plugin:

| State | Meaning |
|---|---|
| `UPDATE AVAILABLE` | The pinned repository version is newer than the installed JAR |
| `up to date` | The installed version is the same or newer than the pinned version |
| `status unknown` | The version could not be determined (rolling "latest" URL with no `Last-Modified` header, network failure, or missing version metadata) |

Pinned entries are compared numerically (so `1.20.4 < 1.20.10` and `5.11.1-SNAPSHOT+1046`
equals `5.11.1`). Rolling "latest" entries compare the remote `Last-Modified` timestamp
with the local JAR's modification time.

`/ob update` forces a download even for entries whose update state is `status unknown`.
Automatic updates (`auto-update: true` and `/ob update` without a forced state) only replace
JARs whose state is `UPDATE AVAILABLE`, so an unknown state never triggers a pointless
re-download.

---

## API

OmniBridge exposes an external API that allows other plugins to interact with the managed plugin ecosystem. Through the API, third-party developers can:

- Query the status of any managed plugin
- Check installed versions
- Programmatically install, update, or remove plugins
- Listen for plugin lifecycle events (`PluginInstalledEvent`, `PluginUpdatedEvent`, `PluginRemovedEvent`)
- Integrate with the preset system

Include OmniBridge as a dependency and reference the API documentation in the project wiki for implementation details.

---

## Supported Languages

OmniBridge ships **72 locale files**. Messages missing from a locale fall back to `en_US`
per key; `msg()` returns the translation rather than leaking a raw message key.
Contributions for additional translations are welcome via pull request.

---

## Velocity

The JAR contains a Velocity entry point (`velocity-plugin.json`), but Velocity support is
**limited**: it registers the `/omnibrige` command and initializes the message manager.
It does **not** download, install, or manage plugins on a proxy. Use a Paper backend for
plugin management, and install ViaVersion/Geyser directly on the proxy if you need them
there.

---

## Development

### Building from Source

OmniBridge is built with Apache Maven.

```bash
git clone https://github.com/enis1enis2/OmniBridge.git
cd OmniBridge
mvn clean package
```

The compiled JAR will be located at `target/OmniBridge-1.0.0.jar`.

### Tests

```bash
mvn test                          # unit tests (network tests excluded)
mvn test -Dtest=RepositoryTest    # single test class
mvn test -DexcludedGroups= -Dtest=RepositoryUrlsTest   # verify every download URL
```

Test counts and metadata assertions live in `src/test/java/io/omnibrige/`. Adding or
removing a registry entry requires updating `RepositoryTest`.

---

## License

This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0).

```
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```
