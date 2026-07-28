# Installation Guide

## Prerequisites

- Java 21 or higher
- Minecraft server (Paper, Spigot, or Velocity 3.4+)
- Internet connection for auto-downloading plugins

## Quick Install (Paper/Spigot)

1. Download `OmniBridge-1.0.0.jar` from the [Releases](https://github.com/enis1enis2/OmniBridge/releases) page
2. Stop your server if it's running
3. Place `OmniBridge-1.0.0.jar` in your server's `plugins/` folder
4. Start your server
5. OmniBridge will detect your platform and auto-install all missing plugins
6. Check console for installation progress
7. Stop and restart the server to load all new plugins
8. Run `/ob setup` to configure which plugins to enable
9. Done! All version support is now active

## Velocity Proxy Install

1. Download `OmniBridge-1.0.0.jar`
2. Place it in Velocity's `plugins/` folder
3. Start the proxy server
4. OmniBridge will install ViaVersion and Geyser on the proxy
5. Restart the proxy

**Important**: If using Velocity with backend servers, install OmniBridge on each backend server as well for full ViaVersion/ViaBackwards support.

## Fabric Install

Geyser and Floodgate have Fabric-specific builds. OmniBridge will download the correct JARs for your platform automatically.

## Post-Installation

### Verify Everything Works

Run in-game:
```
/omnibrige status
```

All plugins should show `ENABLED` status.

### Bedrock Connection

1. Bedrock players connect on port `19132` (default)
2. If `Floodgate` is installed, Bedrock players don't need a Java account
3. If `Floodgate` is NOT installed, Bedrock players need a linked Java account

### Test Bedrock Connection

In console:
```
geyser connectiontest <server-ip> 19132
```

## Configuration

After first run, edit `plugins/OmniBridge/config.yml`:

- Set `bedrock.port` if port 19132 is in use
- Set `java.address` and `java.port` if your Java server isn't on localhost
- Toggle individual plugins on/off in `managed-plugins`

## Troubleshooting

### Plugins not downloading
- Check internet connection
- Verify server has write access to the `plugins/` folder
- Check console for error messages

### Bedrock players can't connect
- Ensure port 19132 (UDP) is open in firewall
- Check Geyser config: `plugins/Geyser-Spigot/config.yml`
- Run: `/omnibrige reload`

### ViaVersion conflicts
- Don't install ViaVersion plugins on both proxy AND backend
- Choose one location (backend recommended for best compatibility)

### Slow startup
- First run downloads plugin JARs, this is normal
- Subsequent startups are fast (plugins already cached)

### Commands Reference

Run `/ob help` in-game to see all available commands. Key commands:

| Command | Description |
|---------|-------------|
| `/ob setup` | Open interactive configuration menu |
| `/ob toggle <plugin>` | Enable/disable a plugin |
| `/ob preset <name>` | Apply a preset (bedrock, essentials, max-compat, etc.) |
| `/ob install` | Download enabled plugins |
| `/ob status` | Check plugin status |
| `/ob info <plugin>` | Show plugin details |
