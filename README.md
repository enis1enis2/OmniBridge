# OmniBridge

**A comprehensive Minecraft server plugin that automatically manages cross-version, cross-platform, and integration plugins for Paper/Bukkit servers and Velocity proxies.**

[![License: GPL-3.0](https://img.shields.io/github/license/enis1enis2/OmniBridge)](https://www.gnu.org/licenses/gpl-3.0)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00)](https://openjdk.org/projects/jdk/21/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8+-00AA00)](https://www.minecraft.net)

---

## Features

- Automatic downloading, configuration, and management of 19+ cross-version and cross-platform plugins
- Interactive chat-based configuration menu for guided setup
- Plugin presets for quick deployment (bedrock, full-version, essentials, max-compat)
- Full internationalization with 68 supported languages
- Automatic dependency resolution across managed plugins
- External API for integration with third-party plugins
- Real-time player version tracking with Bedrock player identification
- Toggle individual plugins on or off without manual file editing
- Centralized update checking and plugin lifecycle management
- Lightweight footprint with minimal performance overhead

---

## Quick Start

### Requirements

- Java 21 or later
- Minecraft server running Paper, Folia, or any Bukkit-compatible fork (1.8+)
- Velocity proxy (optional, for Velocity-specific managed plugins)

### Installation

1. Download the latest `OmniBridge.jar` from the [Releases](https://github.com/enis1enis2/OmniBridge/releases) page.
2. Place the JAR file into your server's `plugins/` directory.
3. Restart or reload your server.
4. Run `/ob setup` in-game or via console to launch the interactive configuration wizard.

---

## Commands

| Command | Description |
|---|---|
| `/ob setup` | Launch the interactive chat-based configuration menu |
| `/ob toggle <plugin>` | Enable or disable a specific managed plugin |
| `/ob preset [name]` | Apply a configuration preset group |
| `/ob install` | Download all currently enabled plugins |
| `/ob update` | Download updates for all managed plugins |
| `/ob check` | Display which managed plugins have available updates |
| `/ob status` | Show the current status of all managed plugins |
| `/ob versions` | List connected player versions (includes Bedrock tags) |
| `/ob reload` | Reload all OmniBridge configuration files |
| `/ob remove <plugin>` | Uninstall a managed plugin from the server |
| `/ob help` | Display available commands and usage information |

---

## Configuration

OmniBridge is configured through a YAML configuration file. Key options include:

| Option | Description |
|---|---|
| `auto-install` | Automatically download plugins when they are enabled |
| `auto-update` | Automatically apply updates when available |
| `locale` | Set the language for chat messages and menus |
| `debug` | Enable verbose logging for troubleshooting |
| `reminder-interval` | How often players are reminded of available updates (in minutes) |
| `managed-plugins` | Per-plugin enable/disable toggles and settings |
| `bedrock` | Network settings for Bedrock Edition player connections |
| `java` | Network settings for Java Edition player connections |

Use `/ob setup` to configure these options interactively, or edit the configuration file directly.

---

## Managed Plugins

### ViaVersion Family (Cross-Version Support)

| Plugin | Description |
|---|---|
| ViaVersion | Allows newer Minecraft client versions to connect to older servers |
| ViaBackwards | Allows older Minecraft client versions to connect to newer servers |
| ViaRewind | Provides backward compatibility for legacy client versions |
| ViaRewindLegacySupport | Extends ViaRewind with support for very old client versions |
| ViaAprilFools | Adds custom protocol version mappings for April Fools Edition |
| ViaBungee | Provides ViaVersion support on BungeeCord proxies |

### GeyserMC Family (Cross-Platform / Bedrock Support)

| Plugin | Description |
|---|---|
| Geyser | Enables Minecraft Bedrock Edition clients to join Java Edition servers |
| Floodgate | Allows Bedrock players to join without requiring a Java Edition license |
| Hurricane | Network compression optimization for Geyser connections |
| GeyserConnect | Proxy-layer connection handling and lobby for Geyser |
| ThirdPartyCosmetics | Enables third-party cosmetic support for Bedrock clients |
| ThunderBeta | Thunder client compatibility layer for Bedrock |
| Rainbow | Visual enhancement and skin support for Bedrock players |

### Integration

| Plugin | Description |
|---|---|
| AuthMe | Registration, login, and authentication management |
| TAB | Player list formatting, prefixes, suffixes, and header/footer customization |

### Community

| Plugin | Description |
|---|---|
| ProtocolLib | Packet-level API used by many plugins for protocol manipulation |
| TuffXPlus | Extended utilities and compatibility fixes for various plugins |

---

## API

OmniBridge exposes an external API that allows other plugins to interact with the managed plugin ecosystem. Through the API, third-party developers can:

- Query the status of any managed plugin
- Check installed and available versions
- Programmatically enable or disable plugins
- Listen for plugin lifecycle events (install, update, remove)
- Integrate with the preset system

Include OmniBridge as a dependency and reference the API documentation in the project wiki for implementation details.

---

## Supported Languages

OmniBridge supports **68 languages** through a comprehensive locale file system. Contributions for additional translations are welcome via pull request.

---

## Development

### Building from Source

OmniBridge is built with Apache Maven.

```bash
git clone https://github.com/enis1enis2/OmniBridge.git
cd OmniBridge
mvn clean package
```

The compiled JAR will be located in the `target/` directory.

---

## License

This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0).

```
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```
