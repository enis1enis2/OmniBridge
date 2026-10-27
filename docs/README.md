# OmniBridge

**A Paper plugin that manages the plugins needed for cross-version and cross-platform Minecraft support.**

OmniBridge automatically downloads, configures, and manages the plugins that let Java clients
from **1.7.2 through 26.3** and **Bedrock Edition** players join your server.

## Features

- **Auto-Installation**: Downloads and installs enabled plugins on first startup
- **Cross-Version Support**: Java 1.7.x to 26.3 via ViaVersion, ViaBackwards, ViaRewind, ViaAprilFools
- **Bedrock Support**: Bedrock Edition via Geyser + Floodgate
- **Auto-Configuration**: Generates default configs for the core managed plugins
- **Interactive Menu**: Chat-based plugin setup with group management and presets
- **Live Status**: View all plugin statuses with a single command
- **Update Management**: Three-state update checking (`UPDATE AVAILABLE`, `up to date`, `status unknown`)
- **Safe Downloads**: Temp file, JAR validation, size cap, and atomic move
- **27 Managed Plugins**: ViaVersion family, GeyserMC family, integrations, and community utilities
- **72 Locales**: Per-key fallback to `en_US`

## Platform Support

| Platform | Status |
|---|---|
| Paper 1.20+ | ✅ Full support |
| Spigot / Folia / Fabric | ❌ Not supported |
| Velocity | ⚠️ Command-only stub (no plugin management) |

## Supported Client Versions

| Client Version | Plugin(s) Used |
|---|---|
| Java 1.7.2 – 1.7.10 | ViaRewind + ViaBackwards + ViaVersion |
| Java 1.8.x | ViaRewind + ViaBackwards + ViaVersion |
| Java 1.9 – 1.20.x | ViaBackwards + ViaVersion |
| Java 1.21 – 1.21.11 | ViaBackwards + ViaVersion |
| Java 26.1 – 26.3 | ViaVersion (forward compatibility) |
| 3D Shareware Snapshot | ViaAprilFools + ViaVersion |
| Combat Test 8c | ViaAprilFools + ViaVersion |
| 20w14infinite | ViaAprilFools + ViaVersion |
| Bedrock Edition | Geyser + Floodgate |

## Managed Plugins

### ViaVersion Family

| Plugin | Description | Pinned version |
|--------|-------------|----------------|
| ViaVersion | Allow newer Java clients on older servers | 5.11.1 |
| ViaBackwards | Allow older Java clients on newer servers | 5.11.1 |
| ViaRewind | Allow 1.7.x/1.8.x clients on newer servers | 4.1.4 |
| ViaRewindLegacySupport | Extra fixes for ViaRewind (Paper only) | 1.5.5 |
| ViaAprilFools | Support for notable Minecraft snapshots | 4.2.3 |
| ViaBungee | ViaVersion for Waterfall/BungeeCord proxies | 0.4.0 (skipped on Paper) |

### GeyserMC Family

| Plugin | Description | Installed to |
|--------|-------------|--------------|
| Geyser | Bridge Bedrock Edition to Java servers | `plugins/` |
| Floodgate | Bedrock auth bypass (no Java account needed) | `plugins/` |
| Hurricane | Server workarounds for Geyser players | `plugins/` |
| GeyserConnect | Bedrock players join without a proxy | `plugins/Geyser-Spigot/extensions/` |
| ThirdPartyCosmetics | Third-party cosmetics support | `plugins/Geyser-Spigot/extensions/` |

Rainbow and ThunderBeta are not managed because they are not server plugins.

### Integration Plugins

| Plugin | Description | Pinned version |
|--------|-------------|----------------|
| AuthMe | Login/authentication with native Bedrock support via Floodgate | 6.0.0 |
| TAB | Tab list, sidebar scoreboard, nametag formatting | 5.0.7 |
| LuckPerms | Permissions management | latest |
| EssentialsX | General server commands and utilities | 2.22.0 |
| PlaceholderAPI | Placeholder expansion API | 2.12.3 |
| WorldGuard | Region protection and flags | 7.0.17 |
| CoreProtect | Block/container logging and rollback | 24.0 |
| DiscordSRV | Bridge Minecraft chat with Discord | 1.30.5 |
| Vault | Economy/permission abstraction | 1.7.3 |

### Community Plugins

| Plugin | Description | Pinned version |
|--------|-------------|----------------|
| ProtocolLib | Packet-level API for protocol manipulation | 5.4.0 |
| TuffXPlus | Modern blocks & entities for TuffClient | 1.1.1 |
| spark | CPU/memory profiling and server health reporting | 1.10.194 |
| Chunky | Pre-generate chunks quickly and efficiently | 1.5.3 |
| BlueMap | 3D web map renderer for Minecraft worlds | 5.22 |
| GriefPrevention | Land claims and anti-grief protection system | 16.18.4 |
| CommandAPI | Command registration API | 12.1.0 |

## Quick Start

1. Download `OmniBridge-1.0.0.jar`
2. Place it in your server's `plugins/` folder
3. Start the server
4. Run `/ob setup` and enable the plugins you need (all are disabled by default)
5. Run `/ob install`, or restart with `auto-install: true`
6. Restart the server to load the new plugins
7. Bedrock players can connect on port `19132` (UDP)

## Commands

| Command | Description |
|---------|-------------|
| `/omnibrige setup` | Open interactive chat-based configuration menu |
| `/omnibrige toggle <plugin>` | Enable or disable a specific managed plugin |
| `/omnibrige preset [name]` | Apply a configuration preset group |
| `/omnibrige install` | Download & install all missing plugins |
| `/omnibrige update` | Check for & apply updates (forces unknown states) |
| `/omnibrige check` | Show update state per plugin |
| `/omnibrige status` | Show all plugin statuses |
| `/omnibrige info <plugin>` | Show version, install target, and description |
| `/omnibrige versions` | Show connected player versions |
| `/omnibrige reload` | Reload config and locale, then reload managed plugins |
| `/omnibrige remove <plugin>` | Remove a managed plugin |
| `/omnibrige enable-all` | Enable all managed plugins |
| `/omnibrige disable-all` | Disable all managed plugins |
| `/omnibrige help` | Show help |

**Aliases:** `/ob`, `/omnib`

## Configuration

Edit `plugins/OmniBridge/config.yml`:

```yaml
auto-install: true     # Auto-install enabled plugins on startup
auto-update: false     # Auto-update plugins on startup

managed-plugins:
  viaversion: false
  viabackwards: false
  viarewind: false
  viarewind-legacysupport: false
  viaprilfools: false
  viabungee: false
  geyser: false
  floodgate: false
  hurricane: false
  geyserconnect: false
  thirdpartycosmetics: false
  authme: false
  tab: false
  protocolib: false
  tuffxplus: false
  luckperms: false
  essentialsx: false
  placeholderapi: false
  worldguard: false
  coreprotect: false
  spark: false
  discordsrv: false
  chunky: false
  bluemap: false
  griefprevention: false
  vault: false
  commandapi: false

bedrock:
  address: 0.0.0.0
  port: 19132

java:
  address: 127.0.0.1
  port: 25565

locale: en_US
debug: false

reminder:
  interval-minutes: 5
```

## Requirements

- Java 21+
- Paper 1.20+
- Internet connection (for auto-download)

## Building from Source

```bash
git clone https://github.com/enis1enis2/OmniBridge.git
cd OmniBridge
mvn clean package
```

Produces: `target/OmniBridge-1.0.0.jar`

Run tests with `mvn test`. The `RepositoryUrlsTest` network smoke test is excluded by
default; run it with `mvn test -DexcludedGroups= -Dtest=RepositoryUrlsTest`.

## License

GPL-3.0
