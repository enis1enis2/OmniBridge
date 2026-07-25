# OmniBridge

**Universal cross-version and cross-platform connectivity solution for Minecraft servers.**

OmniBridge is a server plugin that automatically downloads, configures, and manages all the plugins needed to support every Minecraft client version from **Java 1.7 through 26.2+** and **Bedrock Edition**.

## Features

- **Auto-Installation**: Automatically downloads and installs all required plugins on first startup
- **Cross-Version Support**: Java 1.7.x to 26.2+ via ViaVersion, ViaBackwards, ViaRewind, ViaAprilFools
- **Bedrock Support**: Bedrock Edition via Geyser + Floodgate
- **Auto-Configuration**: Generates optimized configs for all managed plugins
- **Live Status**: View all plugin statuses with a single command
- **Update Management**: Check and apply updates for all managed plugins
- **Multi-Platform**: Works on Paper, Spigot, Velocity, and Fabric

## Supported Client Versions

| Client Version | Plugin(s) Used |
|----------------|----------------|
| Java 1.7.x | ViaRewind + ViaBackwards + ViaVersion |
| Java 1.8.x | ViaBackwards + ViaVersion |
| Java 1.9.x - 1.21.x | ViaBackwards + ViaVersion |
| Java 1.22.x - 26.2+ | ViaVersion (forward compatibility) |
| 3D Shareware Snapshot | ViaAprilFools |
| Combat Test 8c | ViaAprilFools |
| 20w14infinite | ViaAprilFools |
| Bedrock Edition | Geyser + Floodgate |

## Managed Plugins

### ViaVersion Family
| Plugin | Description | Version |
|--------|-------------|---------|
| ViaVersion | Allow newer Java clients on older servers | 5.11.0 |
| ViaBackwards | Allow older Java clients on newer servers | 5.11.0 |
| ViaRewind | Allow 1.7.x/1.8.x clients on newer servers | 4.1.4 |
| ViaRewindLegacySupport | Extra features for ViaRewind (Paper only) | 1.5.4 |
| ViaAprilFools | Support for notable Minecraft snapshots | 4.2.2 |

### GeyserMC Family
| Plugin | Description |
|--------|-------------|
| Geyser | Bridge Bedrock Edition to Java servers |
| Floodgate | Bedrock auth bypass (no Java account needed) |
| Hurricane | Server workarounds for Geyser players |
| GeyserConnect | Bedrock players join without proxy |
| ThirdPartyCosmetics | Third-party cosmetics support |
| ThunderBeta | Java to Bedrock resource pack converter |
| Rainbow | Custom item mapping generator |

## Quick Start

1. Download `OmniBridge-1.0.0.jar`
2. Place it in your server's `plugins/` folder
3. Start the server
4. OmniBridge will auto-install all missing plugins
5. Restart the server to load the new plugins
6. Bedrock players can connect on port `19132`

## Commands

| Command | Description |
|---------|-------------|
| `/omnibrige install` | Download & install all missing plugins |
| `/omnibrige update` | Check for & apply updates |
| `/omnibrige status` | Show all plugin statuses |
| `/omnibrige versions` | Show connected player versions |
| `/omnibrige reload` | Reload all plugin configs |
| `/omnibrige remove <plugin>` | Remove a managed plugin |
| `/omnibrige help` | Show help |

**Aliases:** `/ob`, `/omnib`

## Configuration

Edit `plugins/OmniBridge/config.yml`:

```yaml
auto-install: true    # Auto-install missing plugins on startup
auto-update: false    # Auto-update plugins on startup

managed-plugins:
  viaversion: true
  viabackwards: true
  viarewind: true
  viarewind-legacysupport: true
  viaprilfools: true
  geyser: true
  floodgate: true
  hurricane: true
  geyserconnect: true
  thirdpartycosmetics: true
  thunderbeta: true
  rainbow: true

bedrock:
  address: 0.0.0.0
  port: 19132

java:
  address: 127.0.0.1
  port: 25565
```

## Platform Support

| Platform | Status |
|----------|--------|
| Paper | Full support |
| Spigot | Full support |
| Velocity | Proxy-level support |
| Fabric | Via Geyser-Fabric / Floodgate-Fabric |

## Requirements

- Java 21+
- Minecraft Server 1.8+ (Paper/Spigot) or Velocity 3.4+
- Internet connection (for auto-download)

## Building from Source

```bash
git clone https://github.com/omnibrige/OmniBridge.git
cd OmniBridge
mvn clean package
```

Produces: `target/OmniBridge-1.0.0.jar`

## License

GPL-3.0
