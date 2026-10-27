# Installation Guide

## Prerequisites

- **Java 21 or higher**
- **Paper 1.20 or newer** (OmniBridge uses the Adventure API and declares `api-version: 1.20`)
- Internet access on the server if you want OmniBridge to download plugins

> OmniBridge is a Paper plugin. It is not supported on Spigot, Folia, or Fabric. The JAR
> contains a minimal Velocity entry point, but proxy-side support is command-only — see
> [Velocity](#velocity) below.

## Install (Paper)

1. Download `OmniBridge-1.0.0.jar` from the [Releases](https://github.com/enis1enis2/OmniBridge/releases) page.
2. Stop the server.
3. Place the JAR in the server's `plugins/` directory.
4. Start the server. OmniBridge creates `plugins/OmniBridge/config.yml`.
5. Enable the plugins you need with `/ob setup` (interactive) or `/ob toggle <plugin>`.
   All managed plugins are **disabled by default**.
6. Run `/ob install` to download the enabled plugins, or restart with
   `auto-install: true` (the default) to download them on startup.
7. Restart the server so the new plugin JARs are loaded.

After a restart, `/ob status` should show the managed plugins as `ENABLED`.

### Install locations

| Artifact type | Destination |
|---|---|
| Regular plugins | `plugins/<jarName>` |
| Geyser extensions (GeyserConnect, ThirdPartyCosmetics) | `plugins/Geyser-Spigot/extensions/<jarName>` |

Geyser must already be installed and enabled before its extensions can load.

## Velocity

The JAR is also recognized by Velocity, but Velocity support is **limited**:

- Registers `/omnibrige` (aliases `/ob`, `/omnib`) and loads localized messages.
- Does **not** download, install, update, or manage plugins.

Install OmniBridge on a Paper backend if you want plugin management. If you need
ViaVersion or Geyser on the proxy itself, install those directly.

## Post-Installation

### Verify Everything Works

```
/omnibrige status
```

Installed and loaded plugins show `ENABLED`; downloaded-but-not-loaded plugins show
`INSTALLED`; missing plugins show `MISSING`.

```
/omnibrige check
```

`check` reports one of three states per installed plugin:

| State | Meaning |
|---|---|
| `UPDATE AVAILABLE` | A newer build is available |
| `up to date` | Installed build is current |
| `status unknown` | Version could not be determined (rolling URL without `Last-Modified`, or network failure) |

Use `/ob update` to force a download for entries whose state is unknown.

### Bedrock Connection

1. Bedrock players connect on port `19132` (default, UDP).
2. With Floodgate installed, Bedrock players do not need a Java account.
3. Without Floodgate, Bedrock players need a linked Java account.

### Test Bedrock Connection

In the console:

```
geyser connectiontest <server-ip> 19132
```

## Configuration

After the first run, edit `plugins/OmniBridge/config.yml`:

| Key | Meaning |
|---|---|
| `auto-install` | Download enabled plugins on startup |
| `auto-update` | Apply available updates on startup |
| `managed-plugins.<key>` | Per-plugin enable/disable toggle |
| `bedrock.address` / `bedrock.port` | Geyser Bedrock listener |
| `java.address` / `java.port` | Java server Geyser connects to |
| `locale` | Message locale, e.g. `en_US` or `tr_TR` |
| `debug` | Verbose logging |
| `reminder.interval-minutes` | Reminder interval in minutes (minimum 1) |

Validate and apply changes with `/ob reload`. Note that `/ob reload` reloads plugin
configs and re-enables managed plugins; newly downloaded JARs still require a server
restart.

## Troubleshooting

### Plugins not downloading

- Check the server's internet access and that `plugins/` is writable.
- Check the console for `HTTP <status>` or `Could not fetch Last-Modified` warnings.
- Downloads are rejected if they are not a valid JAR, exceed 128 MB, or use a non-HTTPS URL.
- A failed download leaves any existing JAR untouched (temp file + atomic move).

### Bedrock players can't connect

- Ensure UDP port `19132` is open in the firewall.
- Check Geyser's config at `plugins/Geyser-Spigot/config.yml`.
- Run `/omnibrige reload`.

### ViaVersion conflicts

- Do not install ViaVersion on both the proxy and the backend.
- Choose one location (backend recommended for best compatibility).

### Slow startup

- The first run downloads plugin JARs; this is expected.
- Subsequent startups are fast because the JARs already exist.

### Plugin stuck as "INSTALLED"

The JAR is on disk but not loaded. Restart the server, or run `/ob reload` if the plugin
was already loaded once.

## Commands Reference

Run `/ob help` in-game to see all commands. Key commands:

| Command | Description |
|---|---|
| `/ob setup` | Open the interactive configuration menu |
| `/ob toggle <plugin>` | Enable/disable a plugin |
| `/ob preset <name>` | Apply a preset (`bedrock`, `full-version`, `essentials`, `server-essentials`, `max-compat`, `performance`, `chat`) |
| `/ob install` | Download enabled plugins |
| `/ob update` | Check for and download updates (forces unknown states) |
| `/ob check` | Show update state |
| `/ob status` | Check plugin status |
| `/ob info <plugin>` | Show plugin details, version, and install target |
| `/ob remove <plugin>` | Uninstall a managed plugin |
