# Compatibility Matrix

## OmniBridge Itself

| Requirement | Value |
|---|---|
| Server platform | Paper 1.20+ |
| Java | 21+ |
| Plugin descriptor | `api-version: 1.20` |
| Built against | Paper 1.21.4 API |

OmniBridge is a **Paper plugin**. It does not run on Spigot, Folia, Fabric, or as a mod.
The JAR contains a minimal Velocity entry point, but Velocity only gets the `/omnibrige`
command and localized messages — no plugin management.

## Client Versions

OmniBridge does not itself accept client connections; the managed plugins do. This table
shows which managed plugin combination covers each client range when OmniBridge is used on
a Paper server.

| Client Version | Plugins | Notes |
|---|---|---|
| Bedrock Edition | Geyser + Floodgate | Full translation layer |
| Java 1.7.2 – 1.7.10 | ViaVersion + ViaBackwards + ViaRewind | ViaRewind handles legacy |
| Java 1.8.x | ViaVersion + ViaBackwards + ViaRewind | Most common legacy version |
| Java 1.9 – 1.20.x | ViaVersion + ViaBackwards | |
| Java 1.21 – 1.21.11 | ViaVersion + ViaBackwards | |
| Java 26.1 – 26.3 | ViaVersion | Forward compatibility |
| 3D Shareware / Combat Test 8c / 20w14infinite | ViaAprilFools + ViaVersion | Snapshot support |

## ViaVersion Family

Column values describe whether the artifact is installable by OmniBridge on a Paper server.
Version support for clients is provided by the plugins themselves.

| Plugin | Paper 1.20+ | Notes |
|---|:---:|---|
| ViaVersion | ✅ | Core cross-version layer |
| ViaBackwards | ✅ | Requires ViaVersion |
| ViaRewind | ✅ | Requires ViaVersion + ViaBackwards |
| ViaRewindLegacySupport | ✅ | Requires ViaRewind; Paper only |
| ViaAprilFools | ✅ | Requires ViaVersion |
| ViaBungee | — | Proxy artifact; skipped on Bukkit-based platforms |

## GeyserMC Family

| Plugin | Install target | Notes |
|---|---|---|
| Geyser | `plugins/` | Requires Floodgate |
| Floodgate | `plugins/` | |
| Hurricane | `plugins/` | Requires Geyser + Floodgate |
| GeyserConnect | `plugins/Geyser-Spigot/extensions/` | Geyser extension, requires Geyser |
| ThirdPartyCosmetics | `plugins/Geyser-Spigot/extensions/` | Geyser extension, requires Geyser |

Rainbow (Fabric client mod) and ThunderBeta (resource pack converter) are **not** managed by
OmniBridge because they are not server plugins.

## Integration Plugins

| Plugin | Purpose | Notes |
|---|---|---|
| AuthMe | Login/authentication with native Bedrock support via Floodgate | Auto-enables Floodgate |
| TAB | Tab list, sidebar scoreboard, nametag formatting | Works with ViaVersion/Geyser |
| LuckPerms | Permissions management | |
| EssentialsX | General server commands and utilities | |
| PlaceholderAPI | Placeholder expansion API | |
| WorldGuard | Region protection and flags | |
| CoreProtect | Block/container logging and rollback | |
| DiscordSRV | Bridge Minecraft chat with Discord | Requires PlaceholderAPI |
| Vault | Economy/permission abstraction layer | |

## Community Plugins

| Plugin | Purpose | Notes |
|---|---|---|
| ProtocolLib | Protocol-level packet access | Bukkit-based platforms only |
| TuffXPlus | Modern blocks & entities for TuffClient | Requires ViaVersion + ViaBackwards |
| spark | CPU/memory profiling and server health reporting | |
| Chunky | Chunk pre-generation | |
| BlueMap | 3D web map renderer | |
| GriefPrevention | Land claims and anti-grief protection | |
| CommandAPI | Command registration API for newer Minecraft versions | |

## Update Checking

`/ob check` distinguishes three states:

| State | Detection |
|---|---|
| `UPDATE AVAILABLE` | Pinned version is newer than the installed JAR, or the remote `Last-Modified` is newer than the local file |
| `up to date` | Installed version is equal to or newer than the pinned version, or the local file is at least as new as the remote artifact |
| `status unknown` | Version could not be parsed, the remote server exposed no `Last-Modified` header, or the request failed |

Pinned versions are compared numerically, so `1.20.4 < 1.20.10`, `v1.30.5 == 1.30.5`, and
`5.11.1-SNAPSHOT+1046 == 5.11.1`. Rolling "latest" entries (Geyser, Floodgate, Hurricane,
LuckPerms, Geyser extensions) carry no pinned version and use the timestamp comparison.

## Platform Detection

OmniBridge probes for marker classes in this order:

| Detection order | Marker class | Result |
|---|---|---|
| 1 | `com.velocitypowered.api.proxy.ProxyServer` | Velocity |
| 2 | `io.papermc.paper.plugin.lifecycle.LifecycleEventManager` | Paper |
| 3 | `com.destroystokyo.paper.PaperConfig` | Paper (legacy builds) |
| 4 | `net.fabricmc.loader.api.FabricLoader` | Fabric |
| 5 | `org.bukkit.craftbukkit.CraftServer` or `org.bukkit.Bukkit` | Spigot |
| — | No match | Unknown |

## Presets

| Preset | Description | Included Plugins |
|---|---|---|
| `bedrock` | Bedrock support | Geyser, Floodgate, Hurricane |
| `full-version` | Java version support | ViaVersion, ViaBackwards, ViaRewind, ViaRewindLegacySupport |
| `essentials` | Server essentials | AuthMe, TAB, ProtocolLib |
| `server-essentials` | Permissions and server tools | LuckPerms, EssentialsX, PlaceholderAPI, WorldGuard, CoreProtect |
| `max-compat` | Maximum compatibility | All version + Bedrock + essential + utility plugins |
| `performance` | Performance tools | spark, Chunky, BlueMap |
| `chat` | Chat & Discord | DiscordSRV, PlaceholderAPI |

## Known Conflicts

| Issue | Solution |
|---|---|
| ViaVersion on both proxy and backend | Remove it from one location (backend recommended) |
| SkinRestorer + ViaVersion | Update SkinRestorer or remove it |
