# Compatibility Matrix

## ViaVersion Family

| Server Version | ViaVersion | ViaBackwards | ViaRewind | ViaRewindLegacySupport | ViaAprilFools | ViaBungee |
|----------------|:----------:|:------------:|:---------:|:----------------------:|:-------------:|:---------:|
| Paper 1.8      | ✅ | ❌ | ✅ | ✅ | ✅ | — |
| Paper 1.9      | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.10     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.11     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.12     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.13     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.14     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.15     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.16     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.17     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.18     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.19     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.20     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.21     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 1.22+    | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Paper 26.2     | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| Velocity 3.4   | ✅ | ✅ | ✅ | ❌ | ✅ | — |
| Velocity 3.5   | ✅ | ✅ | ✅ | ❌ | ✅ | — |
| Waterfall       | — | — | — | — | — | ✅ |
| BungeeCord      | — | — | — | — | — | ✅ |

## GeyserMC Family

| Server Platform | Geyser | Floodgate | Hurricane | GeyserConnect | ThirdPartyCosmetics | ThunderBeta | Rainbow |
|-----------------|:------:|:---------:|:---------:|:-------------:|:-------------------:|:-----------:|:-------:|
| Paper 1.13+     | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Spigot 1.13+    | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Velocity        | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Fabric 26.2     | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| NeoForge 26.2   | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

## Community Plugins

| Plugin | Purpose | Platforms |
|--------|---------|-----------|
| ProtocolLib | Protocol-level packet access (required by many anti-cheats and compatibility plugins) | Paper, Spigot |
| TuffXPlus | Modern blocks & entities for TuffClient (Eaglercraft 1.12) | Paper, Spigot |
| spark | CPU/memory profiling and server health reporting | Paper, Spigot, Velocity |
| Chunky | Pre-generate chunks quickly and efficiently | Paper, Spigot, Fabric, Forge |
| BlueMap | 3D web map renderer for Minecraft worlds | Paper, Spigot, Fabric, Forge |
| GriefPrevention | Land claims and anti-grief protection system | Paper, Spigot, Purpur |

## Integration Plugins

| Plugin | Purpose | Platforms | Notes |
|--------|---------|-----------|-------|
| AuthMe | Login/authentication with native Bedrock support via Floodgate 6.0.0 | Paper, Spigot | Requires Floodgate when Bedrock players connect |
| TAB | Tab list, sidebar scoreboard, nametag formatting (all-in-one) | Paper, Spigot | Works with ViaVersion/Geyser |
| DiscordSRV | Bridge Minecraft chat with Discord servers | Paper, Spigot, Purpur | Optional PlaceholderAPI dependency |

## Client Version Support

| Client Version | Connection Method | Notes |
|----------------|-------------------|-------|
| Bedrock Edition | Geyser + Floodgate | Full translation layer |
| Java 1.7.x | ViaRewind + ViaBackwards + ViaVersion | ViaRewind handles legacy |
| Java 1.8.x | ViaBackwards + ViaVersion | Most common legacy version |
| Java 1.9.x | ViaBackwards + ViaVersion | |
| Java 1.10.x | ViaBackwards + ViaVersion | |
| Java 1.11.x | ViaBackwards + ViaVersion | |
| Java 1.12.x | ViaBackwards + ViaVersion | |
| Java 1.13.x | ViaBackwards + ViaVersion | |
| Java 1.14.x | ViaBackwards + ViaVersion | |
| Java 1.15.x | ViaBackwards + ViaVersion | |
| Java 1.16.x | ViaBackwards + ViaVersion | |
| Java 1.17.x | ViaBackwards + ViaVersion | |
| Java 1.18.x | ViaBackwards + ViaVersion | |
| Java 1.19.x | ViaBackwards + ViaVersion | |
| Java 1.20.x | ViaBackwards + ViaVersion | |
| Java 1.21.x | ViaBackwards + ViaVersion | |
| Java 1.22.x+ | ViaVersion | Forward compat only |
| 3D Shareware | ViaAprilFools + ViaVersion | Snapshot support |
| Combat Test 8c | ViaAprilFools + ViaVersion | Snapshot support |
| 20w14infinite | ViaAprilFools + ViaVersion | April Fools snapshot |

## Platform Detection

OmniBridge auto-detects your server platform:

| Detection Method | Platform |
|------------------|----------|
| `com.velocitypowered.api.proxy.ProxyServer` exists | Velocity |
| `io.papermc.paper` exists | Paper |
| `net.fabricmc.loader` exists | Fabric |
| `org.bukkit.craftbukkit` exists | Spigot |
| Fallback | SPIGOT |

## Presets

OmniBridge includes several plugin presets for quick setup:

| Preset | Description | Included Plugins |
|--------|-------------|------------------|
| `bedrock` | Bedrock support | Geyser, Floodgate, Hurricane |
| `full-version` | Java version support | ViaVersion, ViaBackwards, ViaRewind, ViaRewindLegacySupport |
| `essentials` | Server essentials | AuthMe, TAB, ProtocolLib |
| `server-essentials` | Advanced server tools | LuckPerms, EssentialsX, PlaceholderAPI, WorldGuard, CoreProtect |
| `max-compat` | Maximum compatibility | All plugins above + spark, DiscordSRV, Chunky, BlueMap, GriefPrevention |
| `performance` | Performance tools | spark, Chunky, BlueMap |
| `chat` | Chat & Discord | DiscordSRV, PlaceholderAPI |

## Known Conflicts

| Issue | Solution |
|-------|----------|
| ViaVersion on both proxy and backend | Remove from one location (backend recommended) |
| SkinRestorer + ViaVersion | Update SkinRestorer or remove it |
| BKCommonLib 1.8.8 | Use special build from OmniBridge docs |
| Orebfuscator 1.8 | Use special build from OmniBridge docs |
